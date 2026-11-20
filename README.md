# denlib

Shared UI library for DenLille_'s Minecraft mods (Bounded Worlds, World Difficulty).

It adds **choice rows** to the **World** tab of the Create New World screen, in plain vanilla style,
and keeps that tab usable when several mods add content to it.

## Features

- **World tab sections**: mods register a section; denlib injects it once, in a stable order.
- **Choice rows**: a title, vanilla buttons (3 per line, the picked one shown pressed), an optional
  description under the buttons and optional tooltips.
- **Required choices**: clicking *Create New World* without picking turns the title red, shows the
  tab and scrolls to the row — creation is never blocked if a section could not be injected.
- **Auto-scroll**: a creation tab whose content overflows (e.g. GUI scale 4) gets a vanilla-style
  scrollbar, mouse wheel and drag; content is clipped cleanly and hidden buttons are not clickable.
- **Data pack aware**: sections are rebuilt when the player changes data packs from the More tab.

## Using it from a mod

```java
CreateWorldSections.register(new ResourceLocation("mymod", "size"), 10, ctx ->
        ChoiceRow.<Size>builder(ctx, new ResourceLocation("mymod", "size"), Component.translatable("mymod.size"))
                .option(Size.SMALL, Component.translatable("mymod.size.small"))
                    .tooltip(Component.translatable("mymod.size.small.tooltip"))
                .option(Size.LARGE, Component.translatable("mymod.size.large"))
                .description(size -> Component.translatable("mymod.size." + size.id() + ".desc"))
                .onCreate(size -> MyMod.armPendingSize(size))
                .build());
```

Register sections from client code only (e.g. `FMLClientSetupEvent`). denlib only has client-side
content: declare the dependency with `side="CLIENT"` in your `mods.toml`.

## Supported versions

- Minecraft **1.20.1**
- Forge **47.3.4** or newer
- Client side

## License

[MIT](LICENSE)
