# denlib — Document de design

**Date :** 2026-11-20
**Statut :** Validé avec l'auteur (conversation du 2026-11-20)
**Cible :** Minecraft Forge 1.20.1 (47.3.4+) — mod id `denlib`, package `net.denlille.denlib`
**Consommateurs :** Bounded Worlds (migration) puis World Difficulty (nouvelle UI)

---

## 1. Pourquoi

World Difficulty et Bounded Worlds ajoutent chacun un choix à l'écran de création de monde. L'auteur veut
une UI **vanilla et simple** pour les deux : une section dans l'onglet **World** (titre + rangée de boutons
vanilla, le choisi enfoncé), comme le sélecteur de taille de Bounded Worlds aujourd'hui.

Deux sections dans l'onglet World + le contenu vanilla peuvent dépasser la hauteur disponible (GUI scale 4
en 1080p ≈ 210 px utiles). Les onglets 1.20.1 ne scrollent pas (`GridLayoutTab.doLayout` aligne et c'est
tout). Il faut donc un **scroll quand ça dépasse**, et ce scroll doit exister **une seule fois** au runtime :
deux mixins de scroll concurrents sur le même onglet se battraient. D'où une lib partagée.

## 2. Décisions actées

| Sujet | Décision |
|---|---|
| Distribution | **Mod séparé** (`denlib`), dépendance obligatoire déclarée par chaque mod consommateur (pas de Jar-in-Jar) — choix de l'auteur, pour mieux gérer la lib elle-même |
| Version | Forge 1.20.1 uniquement pour l'instant |
| Côté | Client uniquement en pratique (tout le contenu est de l'UI), mais le mod se charge des deux côtés sans rien faire côté serveur |
| Périmètre | Les 4 briques ci-dessous, rien d'autre (pas d'autres écrans, pas de config) |

## 3. Les 4 briques

### 3.1 Sections de l'onglet World

- Les mods enregistrent une **section** : `CreateWorldSections.register(ResourceLocation id, int order, SectionFactory factory)`.
  À appeler au setup client (ou en statique) ; l'enregistrement est global, pas par écran.
- La lib injecte **une seule fois** dans le constructeur de `CreateWorldScreen$WorldTab` : au `TAIL`, elle
  construit chaque section (tri par `order` croissant, puis par id) et l'ajoute au `RowHelper` 2 colonnes
  de l'onglet sur toute la largeur (`addChild(element, 2)`), comme Bounded Worlds aujourd'hui.
- Récupération du `RowHelper` : capture du **premier** local de type `GridLayout.RowHelper` stocké dans le
  constructeur (`@ModifyVariable(at = @At("STORE"), ordinal = 0)`), mémorisé dans un champ `@Unique`.
  Plus robuste que `LocalCapture` (pas de dépendance à la liste complète des locaux).
- `require = 0` sur ces injections : si un autre mod remanie le constructeur, les sections ne s'ajoutent pas,
  un warning est loggé, et **la création n'est jamais bloquée** (voir 3.3).
- `SectionFactory` reçoit un `SectionContext` (l'écran, son `WorldCreationUiState`) et renvoie un
  `LayoutElement` (ou `null` pour ne rien ajouter).

### 3.2 Rangée de choix (`ChoiceRow<T>`)

Construction par builder :

```java
ChoiceRow<WorldSize> row = ChoiceRow.<WorldSize>builder(ctx, MY_ID, Component.translatable("…title"))
        .option(WorldSize.SMALL, Component.translatable("…small"))
            .tooltip(Component.translatable("…radius", 1000))
        .option(…)
        .description(value -> Component.translatable(…))   // optionnel : texte sous les boutons
        .required(true)                                     // défaut : true
        .initial(null)                                      // défaut : aucune présélection
        .onSelect(value -> …)                               // optionnel
        .onCreate(value -> …)                               // appelé quand la création passe le gate
        .build();                                           // renvoie un LayoutElement
```

- Rendu : titre (`StringWidget`, aligné à gauche), puis boutons vanilla (`Button`, **100 px** de large,
  5 px d'écart), **retour à la ligne tous les 3 boutons**, puis description optionnelle
  (`MultiLineTextWidget`, largeur max = largeur de la rangée, soit 310 px).
- Le bouton choisi est affiché enfoncé (`active = false`), comme Bounded Worlds.
- Aucune présélection par défaut. **Exception** : une rangée avec **une seule option** est présélectionnée
  automatiquement (et ne bloque donc jamais la création).
- Description : mise à jour à chaque sélection ; comme sa hauteur peut changer, la lib relance la mise en
  page de l'écran (`screen.repositionElements()`), ce qui recalcule aussi le scroll.
- **Persistance par écran** : la sélection est stockée dans un état lié à l'instance de `CreateWorldScreen`
  (map faible écran → (id de rangée → valeur)). Un redimensionnement de la fenêtre (qui rappelle `init()`
  et reconstruit les onglets) **restaure** la sélection et l'état enfoncé. Annuler l'écran jette l'état avec
  lui : aucune fuite vers la création suivante. (Corrige au passage un défaut de Bounded Worlds, qui remet
  sa sélection à zéro à chaque reconstruction de l'onglet.)
- API de lecture : `ChoiceRow.selection(screen, id)` → `Optional<T>`.

### 3.3 Choix obligatoire (gate de création)

- **Une seule injection** sur `CreateWorldScreen.onCreate` (`HEAD`, annulable, `require = 0`) — couvre aussi
  la touche Entrée, qui appelle `onCreate`.
- Si au moins une rangée `required` **présente dans l'écran courant** n'a pas de sélection :
  1. son titre passe en **rouge** (`0xFF5555`) jusqu'à ce qu'une option soit choisie ;
  2. l'onglet qui contient la rangée est sélectionné (recherche du `TabNavigationBar` parmi les enfants de
     l'écran, puis de l'onglet dont `visitChildren` contient le titre) ;
  3. le scroll de cet onglet est ajusté pour rendre le titre visible ;
  4. la création est annulée.
- Toutes les rangées manquantes passent en rouge ; on saute à la première (ordre des sections).
- Si tout est rempli : chaque rangée appelle son `onCreate(value)` puis vanilla continue.
- Une rangée qui n'a pas pu s'injecter n'est pas « présente » → elle ne bloque jamais.

### 3.4 Scroll automatique des onglets

S'applique à tout `GridLayoutTab` affiché dans un `CreateWorldScreen` (onglets vanilla et onglets de mods).

- **Détection** : après `GridLayoutTab.doLayout(area)` (injection `TAIL`), si la hauteur du layout dépasse
  `area.height()`, l'onglet passe en mode scroll ; sinon rien ne change par rapport à vanilla.
- **Mise en page en mode scroll** : contenu centré horizontalement comme vanilla, haut du contenu à
  `area.top + 4 - scroll` ; `scroll` est borné à `[0, hauteurContenu + 8 - area.height]`.
- **Entrée** : roulette (`ScreenEvent.MouseScrolled.Post`, donc seulement si aucun widget n'a consommé
  l'événement), pas de 20 px par cran. **Glisser la barre** à la souris (clic sur la barre + drag).
- **Rendu** :
  - chaque widget de l'onglet scrollé est découpé à la zone de l'onglet (scissor autour de
    `AbstractWidget.render`) → coupe propre sous la barre d'onglets et au-dessus du pied de page ;
  - barre de défilement à droite de la zone, style des listes vanilla (piste noire, curseur gris
    `0xFF808080`/`0xFFC0C0C0`, hauteur proportionnelle).
- **Clics et survol** : quand la souris est hors de la zone de l'onglet, les widgets scrollés ne sont ni
  survolés ni cliquables (coordonnées de souris neutralisées pour ces widgets).
- **État** : un offset par instance d'onglet, conservé au changement d'onglet et recalé (borné) à chaque
  `doLayout` (redimensionnement, changement de hauteur de contenu).
- **Focus clavier** : non géré en v1 (un widget atteint au Tab peut rester hors champ). Noté comme limite.

## 4. Approches envisagées

1. **Lib en mod séparé (retenu).** Une seule implémentation du scroll et des injections au runtime ;
   coût : une dépendance de plus à installer pour les joueurs (à annoncer dans les changelogs).
2. **Lib embarquée en Jar-in-Jar dans chaque mod.** Invisible pour le joueur, Forge ne charge que la plus
   récente ; écarté par l'auteur au profit d'une lib gérée comme un mod à part entière.
3. **Pas de lib (chaque mod fait son UI, scroll dans un seul des deux).** Couplage entre mods et risque de
   double scroll ; écarté.

Pour le scroll lui-même : envelopper les widgets de l'onglet dans un conteneur unique a été écarté (casse
les mods qui cherchent les widgets dans `screen.children()`) ; le `ScrollPanel` de Forge aussi (pas l'aspect
vanilla, gère mal les widgets placés par la grille). Retenu : les widgets restent des enfants normaux de
l'écran, la lib les décale, les découpe et filtre leurs entrées.

## 5. Hors périmètre (v1)

- Autres écrans que `CreateWorldScreen`, sections dans les onglets Game / More.
- Configuration de la lib, commandes, contenu serveur.
- Suivi du focus clavier par le scroll.
- Toute API qu'aucun des deux mods consommateurs n'utilise.

## 6. Laissé au constructeur

- Noms exacts des classes internes, accessors nécessaires (`GridLayoutTab.layout`,
  `CreateWorldScreen.tabManager`/`tabNavigationBar` si la recherche dans `children()` ne suffit pas —
  attention : Bounded Worlds a constaté que l'AP MixinGradle n'émettait pas l'entrée refmap d'un `@Shadow`
  de champ privé sur `CreateWorldScreen` ; préférer la recherche dans `children()` ou un `@Accessor`
  vérifié dans le jar réobfusqué).
- Publication : `maven-publish` vers `mavenLocal()` (`net.denlille:denlib:<version>`), consommée par les mods
  via `fg.deobf(...)` ; attribut de manifeste `MixinConfigs: denlib.mixins.json` pour que les mixins de la lib
  se chargent quand elle est une dépendance.
- Mod de test en dev : une section de démonstration activée uniquement en environnement de dev
  (`!FMLEnvironment.production`) pour valider la lib seule avant la migration de Bounded Worlds.
