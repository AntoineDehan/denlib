# denlib Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Une lib Forge 1.20.1 (`denlib`) qui permet aux mods d'ajouter des sections « rangée de choix » à l'onglet World de l'écran de création de monde, avec choix obligatoire et scroll automatique des onglets qui débordent.

**Architecture:** Logique pure (découpage en lignes, bornes de scroll, géométrie de la barre, store de sélection) testée en JUnit ; trois points d'injection Mixin (constructeur `WorldTab`, `CreateWorldScreen.onCreate`, `GridLayoutTab.doLayout`) + un mixin sur `AbstractWidget` (scissor, neutralisation souris) ; événements Forge pour roulette / drag / rendu de la barre.

**Tech Stack:** Forge 47.3.4, parchment 2023.09.03-1.20.1, Mixin 0.8.5 (MixinGradle), JUnit 5.10, JDK 17.

**Spec:** `docs/superpowers/specs/2026-11-20-denlib-design.md`

## Global Constraints

- mod id `denlib`, package `net.denlille.denlib`, groupe Maven `net.denlille`, artefact `denlib`, version `0.1.0-1.20.1`, licence MIT, auteur `Denlille`.
- Forge `[47,)`, Minecraft `[1.20.1,1.21)`, Java 17.
- Tout fichier source créé commence par `// This file was made with Claude Code`.
- Commits sans trailer `Co-Authored-By`.
- Boutons : 100 px de large, 5 px d'écart, 3 par ligne ; description max 310 px ; titre manquant `0xFF5555`, normal `0xFFFFFF`.
- Scroll : marge 4 px en haut et en bas, 20 px par cran, curseur `0xFF808080` / reflet `0xFFC0C0C0`, piste `0xFF000000`, barre de 6 px.
- Toute injection sur une classe vanilla de l'écran de création : `require = 0` (jamais de crash, jamais de création bloquée si l'injection échoue).

## Review Focus

- Redimensionner la fenêtre après avoir choisi → la sélection et le bouton enfoncé sont conservés (test `SelectionStoreTest.survivesRebuild`).
- Annuler l'écran puis rouvrir « Create New World » → aucune sélection héritée (store faible par écran ; vérif manuelle Task 6).
- Contenu exactement égal à la hauteur de la zone → pas de mode scroll (test `ScrollMathTest.noScrollWhenFits`).
- Contenu qui rétrécit (description plus courte) alors qu'on était scrollé en bas → offset re-borné, pas de vide (test `ScrollMathTest.clampAfterShrink`).
- Clic sur un bouton caché sous le pied de page → n'a aucun effet (vérif manuelle Task 6).

---

### Task 1 : Scaffolding + logique pure (TDD)

**Files:**
- Create: `build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/wrapper/*`, `gradlew(.bat)`, `.gitignore` (copiés de Bounded Worlds puis adaptés)
- Create: `src/main/resources/META-INF/mods.toml`, `pack.mcmeta`, `denlib.mixins.json`
- Create: `src/main/java/net/denlille/denlib/DenLib.java`
- Create: `src/main/java/net/denlille/denlib/client/scroll/ScrollMath.java`
- Create: `src/main/java/net/denlille/denlib/client/choice/ChoiceGrid.java`
- Create: `src/main/java/net/denlille/denlib/client/choice/SelectionStore.java`
- Test: `src/test/java/net/denlille/denlib/client/scroll/ScrollMathTest.java`, `.../choice/ChoiceGridTest.java`, `.../choice/SelectionStoreTest.java`

**Interfaces — Produces:**
- `ScrollMath.maxScroll(int contentHeight, int viewportHeight) -> int` = `max(0, contentHeight + 8 - viewportHeight)` si `contentHeight > viewportHeight`, sinon `0`.
- `ScrollMath.overflows(int contentHeight, int viewportHeight) -> boolean` = `contentHeight > viewportHeight`.
- `ScrollMath.clamp(double offset, int contentHeight, int viewportHeight) -> double`.
- `ScrollMath.thumbHeight(int contentHeight, int viewportHeight) -> int` = `clamp(viewport² / (content + 8), 32, viewport - 8)`.
- `ScrollMath.thumbY(double offset, int contentHeight, int viewportHeight, int top) -> int`.
- `ScrollMath.offsetForThumbDrag(double startOffset, double dragDy, int contentHeight, int viewportHeight) -> double`.
- `ChoiceGrid.rows(int count, int perRow) -> int[]` (taille de chaque ligne).
- `SelectionStore` : `static <T> Optional<T> get(Object screen, ResourceLocation rowId)`, `static void put(Object screen, ResourceLocation rowId, Object value)` ; map `WeakHashMap<Object, Map<ResourceLocation, Object>>`.

- [ ] **Step 1 : Scaffolding** — copier la base Bounded Worlds, remplacer les propriétés (Global Constraints), ajouter `testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'` + `test { useJUnitPlatform() }`, attribut de manifeste `'MixinConfigs': 'denlib.mixins.json'`, `publishing.repositories { mavenLocal() }`, `mixin { add sourceSets.main, 'mixins.denlib.refmap.json'; config 'denlib.mixins.json' }`. `DenLib` = classe `@Mod("denlib")` avec `LOGGER`. `git init`.
- [ ] **Step 2 : Tests qui échouent**
  - `ScrollMathTest.noScrollWhenFits` : `overflows(200,200)==false`, `maxScroll(200,200)==0`.
  - `ScrollMathTest.maxScrollAddsMargins` : `maxScroll(300,200)==108`.
  - `ScrollMathTest.clampAfterShrink` : `clamp(108,250,200)==58`, `clamp(-5,300,200)==0`.
  - `ScrollMathTest.thumbBounds` : `thumbHeight(300,200)==129` (division entière) ; `thumbY(0,300,200,40)==40` ; `thumbY(108,300,200,40)==40+200-129`.
  - `ScrollMathTest.dragMapsTrackToContent` : `offsetForThumbDrag(0, 71, 300, 200) == 108` (toute la course du curseur = tout le scroll).
  - `ChoiceGridTest` : `rows(3,3)=={3}`, `rows(5,3)=={3,2}`, `rows(1,3)=={1}`, `rows(0,3)=={}`.
  - `SelectionStoreTest.survivesRebuild` : `put(screen, id, "a")` puis `get(screen, id)` → `"a"` ; autre écran → vide.
- [ ] **Step 3 : Vérifier l'échec** — `./gradlew test` → FAIL (classes absentes).
- [ ] **Step 4 : Implémenter** les trois classes.
- [ ] **Step 5 : Vérifier** — `./gradlew test build` → BUILD SUCCESSFUL.
- [ ] **Step 6 : Commit** — `chore: scaffold denlib` puis `feat: pure scroll math, choice grid and selection store`.

### Task 2 : Sections de l'onglet World

**Files:**
- Create: `client/section/CreateWorldSections.java`, `client/section/SectionContext.java`, `client/section/SectionFactory.java`
- Create: `mixin/client/WorldTabMixin.java`

**Interfaces — Produces:**
- `CreateWorldSections.register(ResourceLocation id, int order, SectionFactory factory)` ; `static List<Entry> sorted()` (ordre, puis id).
- `@FunctionalInterface SectionFactory { @Nullable LayoutElement create(SectionContext ctx); }`
- `record SectionContext(CreateWorldScreen screen, WorldCreationUiState uiState)`.

- [ ] **Step 1 : Mixin** sur `CreateWorldScreen$WorldTab` : `@ModifyVariable(method="<init>", at=@At("STORE"), ordinal=0, require=0)` sur `GridLayout.RowHelper` → champ `@Unique denlib$rowHelper` ; `@Inject(method="<init>", at=@At("TAIL"), require=0)` → pour chaque section, `factory.create(ctx)` et `rowHelper.addChild(element, 2)` ; exception dans une factory → log warn + section ignorée. `rowHelper` null → log warn une fois.
- [ ] **Step 2 : Vérifier** — `./gradlew build` vert.
- [ ] **Step 3 : Commit** — `feat: World tab section hook`.

### Task 3 : ChoiceRow + gate de création

**Files:**
- Create: `client/choice/ChoiceRow.java` (builder + widget layout), `client/choice/ChoiceRowRegistry.java` (rangées vivantes par écran)
- Create: `mixin/client/CreateWorldScreenMixin.java`
- Create: `client/TabUtil.java` (sélection d'onglet par widget)

**Interfaces:**
- Consumes: `SelectionStore`, `ChoiceGrid.rows`, `SectionContext`.
- Produces: API du §3.2 de la spec, à l'identique : `ChoiceRow.builder(SectionContext ctx, ResourceLocation id, Component title)`, `.option(T, Component)`, `.tooltip(Component)` (s'applique à la dernière option), `.description(Function<T, Component>)`, `.required(boolean)`, `.initial(@Nullable T)`, `.onSelect(Consumer<T>)`, `.onCreate(Consumer<T>)`, `.build() -> LayoutElement` ; `static <T> Optional<T> ChoiceRow.selection(Object screen, ResourceLocation id)`.
- Produces: `ChoiceRowRegistry.live(Object screen) -> List<ChoiceRow<?>>` ; `ChoiceRow#isMissing()`, `#flagMissing()`, `#fireOnCreate()`, `#title() -> StringWidget`.
- Produces: `TabUtil.selectTabContaining(Screen screen, AbstractWidget widget) -> boolean`.

- [ ] **Step 1 : `ChoiceRow`** — sélection initiale : store de l'écran, sinon `initial`, sinon l'unique option si une seule ; à la sélection : store, boutons (`active = value != selected`), titre en blanc, description mise à jour puis `screen.repositionElements()`, `onSelect`. S'enregistre dans `ChoiceRowRegistry` (la liste de l'écran est vidée quand le `WorldTab` est reconstruit — appel depuis le mixin de la Task 2 avant de construire les sections).
- [ ] **Step 2 : Gate** — `@Inject(method="onCreate", at=@At("HEAD"), cancellable=true, require=0)` : rangées `required && isMissing()` → `flagMissing()` sur toutes, `TabUtil.selectTabContaining` + `TabScroll.reveal(title)` (Task 4 ; stub no-op d'ici là) sur la première, `ci.cancel()`. Sinon `fireOnCreate()` sur toutes.
- [ ] **Step 3 : Vérifier** — `./gradlew build` vert.
- [ ] **Step 4 : Commit** — `feat: ChoiceRow sections with required-choice creation gate`.

### Task 4 : Scroll des onglets

**Files:**
- Create: `client/scroll/TabScroll.java` (état par onglet, zone, ensemble des widgets scrollés, rendu barre, entrées)
- Create: `mixin/client/GridLayoutTabMixin.java`, `mixin/client/GridLayoutTabAccessor.java` (`layout`), `mixin/client/AbstractWidgetMixin.java`

**Interfaces:**
- Consumes: `ScrollMath`.
- Produces: `TabScroll.afterLayout(GridLayoutTab tab, GridLayout layout, ScreenRectangle area)`, `TabScroll.reveal(AbstractWidget widget)`, `TabScroll.isClipped(AbstractWidget w) -> boolean`, `TabScroll.area() -> @Nullable ScreenRectangle`.

- [ ] **Step 1 : Layout** — `GridLayoutTabMixin` : `@Inject(method="doLayout", at=@At("TAIL"))` → si l'écran courant est un `CreateWorldScreen`, `TabScroll.afterLayout(...)` : si `overflows`, `layout.setY(area.top() + 4 - (int) offset)` (offset re-borné) et mémorise l'onglet actif, la zone et l'ensemble identitaire de ses widgets (`visitChildren`) ; sinon désactive.
- [ ] **Step 2 : Rendu et souris par widget** — `AbstractWidgetMixin` sur `render(GuiGraphics,int,int,float)` : HEAD → si `isClipped(this)`, `guiGraphics.enableScissor(area)` et, si la souris est hors zone, `@ModifyVariable` des args `mouseX/mouseY` à `-1` ; RETURN → `disableScissor()`. `isMouseOver` et `clicked` → `false` si clippé et souris hors zone.
- [ ] **Step 3 : Événements** (`@Mod.EventBusSubscriber(value = Dist.CLIENT)`) — `ScreenEvent.MouseScrolled.Post` : offset −= delta×20, re-borner, re-placer ; `MouseButtonPressed.Pre` sur la barre → début de drag (sur la piste hors curseur : saut de page) ; `MouseDragged.Pre` → `offsetForThumbDrag` ; `MouseButtonReleased.Pre` → fin ; `ScreenEvent.Render.Post` → barre à `area.right() - 6`. Fermeture d'écran / autre écran → état réinitialisé.
- [ ] **Step 4 : `reveal(widget)`** — ajuste l'offset pour que le widget soit entièrement visible, re-place.
- [ ] **Step 5 : Vérifier** — `./gradlew build` vert.
- [ ] **Step 6 : Commit** — `feat: automatic scroll for overflowing creation tabs`.

### Task 5 : Section de démo (dev) + vérification en jeu + publication

**Files:**
- Create: `client/dev/DevDemoSections.java` (enregistrée seulement si `!FMLEnvironment.production`)
- Modify: `src/main/resources/assets/denlib/lang/en_us.json`, `fr_fr.json`

- [ ] **Step 1 : Démo** — deux sections : « Demo Difficulty » (5 options → 2 lignes, description longue, required) et « Demo Size » (3 options, tooltips, required).
- [ ] **Step 2 : Vérification manuelle** (`./gradlew runClient`, GUI scale 2 puis 4) :
  1. Onglet World : vanilla puis Demo Difficulty puis Demo Size ; en GUI scale 4 la barre apparaît, roulette + drag OK, contenu coupé net sous la barre d'onglets et au-dessus du pied.
  2. Clic sous le pied de page sur un bouton caché → rien.
  3. Créer sans choisir → titres rouges, onglet World affiché, scrollé jusqu'au premier titre rouge.
  4. Choisir, redimensionner la fenêtre → choix conservés.
  5. Annuler, rouvrir → rien de présélectionné.
  6. Créer avec les choix → monde créé, logs `onCreate` de la démo.
- [ ] **Step 3 : Publication** — `./gradlew publishToMavenLocal` → `~/.m2/repository/net/denlille/denlib/0.1.0-1.20.1/` existe.
- [ ] **Step 4 : Commit** — `feat: dev demo sections`.
