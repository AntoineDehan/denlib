// This file was made with Claude Code
package net.denlille.denlib.client.choice;

import net.denlille.denlib.client.CreateWorldState;
import net.denlille.denlib.client.SafeCall;
import org.jetbrains.annotations.ApiStatus;
import net.denlille.denlib.client.section.SectionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A titled row of vanilla buttons, one per option, with the picked one shown pressed and an optional
 * description under the buttons. Built for a World tab section via {@link #builder}.
 */
public final class ChoiceRow<T> {
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_SPACING = 5;
    public static final int BUTTONS_PER_LINE = 3;
    public static final int ROW_WIDTH = BUTTONS_PER_LINE * BUTTON_WIDTH + (BUTTONS_PER_LINE - 1) * BUTTON_SPACING;
    public static final int COLOR_TITLE = 0xFFFFFF;
    public static final int COLOR_MISSING = 0xFF5555;
    public static final int COLOR_DESCRIPTION = 0xA0A0A0;

    private final CreateWorldScreen screen;
    private final ResourceLocation id;
    private final List<Option<T>> options;
    @Nullable
    private final Function<T, Component> description;
    private final boolean required;
    @Nullable
    private final Consumer<T> onSelect;
    @Nullable
    private final Consumer<T> onCreate;
    private final StringWidget title;
    private final List<Button> buttons = new ArrayList<>();
    @Nullable
    private MultiLineTextWidget descriptionWidget;
    @Nullable
    private T selected;

    private ChoiceRow(Builder<T> builder) {
        this.screen = builder.context.screen();
        this.id = builder.id;
        this.options = List.copyOf(builder.options);
        this.description = builder.description;
        this.required = builder.required;
        this.onSelect = builder.onSelect;
        this.onCreate = builder.onCreate;
        this.title = new StringWidget(builder.title, Minecraft.getInstance().font).alignLeft();
        this.title.setColor(COLOR_TITLE);
    }

    public static <T> Builder<T> builder(SectionContext context, ResourceLocation id, Component title) {
        return new Builder<>(context, id, title);
    }

    /** The current selection of row {@code id} on {@code screen}, if any. */
    public static <T> Optional<T> selection(Object screen, ResourceLocation id) {
        return CreateWorldState.of(screen).selection(id);
    }

    public ResourceLocation id() {
        return this.id;
    }

    @ApiStatus.Internal
    public StringWidget title() {
        return this.title;
    }

    @ApiStatus.Internal
    public boolean isRequired() {
        return this.required;
    }

    @ApiStatus.Internal
    public boolean isMissing() {
        return this.selected == null;
    }

    @ApiStatus.Internal
    public void flagMissing() {
        this.title.setColor(COLOR_MISSING);
    }

    @ApiStatus.Internal
    public void fireOnCreate() {
        T value = this.selected;
        if (this.onCreate != null && value != null) {
            SafeCall.run("onCreate of choice row " + this.id, () -> this.onCreate.accept(value));
        }
    }

    private LayoutElement buildLayout() {
        Minecraft minecraft = Minecraft.getInstance();
        GridLayout section = new GridLayout().rowSpacing(4);
        GridLayout.RowHelper rows = section.createRowHelper(1);
        rows.addChild(this.title);

        int index = 0;
        for (int lineSize : ChoiceGrid.rows(this.options.size(), BUTTONS_PER_LINE)) {
            GridLayout line = new GridLayout().columnSpacing(BUTTON_SPACING);
            GridLayout.RowHelper columns = line.createRowHelper(lineSize);
            for (int i = 0; i < lineSize; i++) {
                Option<T> option = this.options.get(index++);
                Button.OnPress onPress = b -> select(option.value(), true);
                ItemStack icon = option.icon() == null ? ItemStack.EMPTY : new ItemStack(option.icon());
                Button button = icon.isEmpty()
                        ? Button.builder(option.label(), onPress).width(BUTTON_WIDTH).build()
                        : new IconButton(BUTTON_WIDTH, option.label(), icon, onPress);
                if (option.tooltip() != null) {
                    button.setTooltip(Tooltip.create(option.tooltip()));
                }
                this.buttons.add(button);
                columns.addChild(button);
            }
            rows.addChild(line);
        }

        if (this.description != null) {
            this.descriptionWidget = new MultiLineTextWidget(Component.empty(), minecraft.font)
                    .setMaxWidth(ROW_WIDTH);
            this.descriptionWidget.setColor(COLOR_DESCRIPTION);
            rows.addChild(this.descriptionWidget);
        }
        return section;
    }

    private void restoreInitial(@Nullable T initial) {
        // A selection whose option no longer exists (data packs changed) is forgotten.
        T start = CreateWorldState.of(this.screen).<T>validSelection(this.id, this::isOption)
                .orElse(null);
        if (start == null && initial != null && isOption(initial)) {
            start = initial;
        }
        if (start == null && this.options.size() == 1) {
            start = this.options.get(0).value();
        }
        if (start != null) {
            select(start, false);
        }
    }

    private boolean isOption(T value) {
        return this.options.stream().anyMatch(option -> Objects.equals(option.value(), value));
    }

    private void select(T value, boolean byPlayer) {
        this.selected = value;
        CreateWorldState.of(this.screen).select(this.id, value);
        for (int i = 0; i < this.options.size(); i++) {
            this.buttons.get(i).active = !Objects.equals(this.options.get(i).value(), value);
        }
        this.title.setColor(COLOR_TITLE);
        if (this.descriptionWidget != null && this.description != null) {
            this.descriptionWidget.setMessage(this.description.apply(value));
        }
        if (byPlayer) {
            if (this.onSelect != null) {
                SafeCall.run("onSelect of choice row " + this.id, () -> this.onSelect.accept(value));
            }
            // The description height may have changed: re-run the layout (and the tab scroll bounds).
            this.screen.repositionElements();
        }
    }

    private record Option<T>(T value, Component label, @Nullable Component tooltip, @Nullable ItemLike icon) {
        Option<T> withTooltip(Component tooltip) {
            return new Option<>(this.value, this.label, tooltip, this.icon);
        }

        Option<T> withIcon(ItemLike icon) {
            return new Option<>(this.value, this.label, this.tooltip, icon);
        }
    }

    public static final class Builder<T> {
        private final SectionContext context;
        private final ResourceLocation id;
        private final Component title;
        private final List<Option<T>> options = new ArrayList<>();
        @Nullable
        private Function<T, Component> description;
        private boolean required = true;
        @Nullable
        private T initial;
        @Nullable
        private Consumer<T> onSelect;
        @Nullable
        private Consumer<T> onCreate;

        private Builder(SectionContext context, ResourceLocation id, Component title) {
            this.context = context;
            this.id = id;
            this.title = title;
        }

        public Builder<T> option(T value, Component label) {
            this.options.add(new Option<>(value, label, null, null));
            return this;
        }

        /** Tooltip of the last added option. */
        public Builder<T> tooltip(Component tooltip) {
            int last = lastOption("tooltip");
            this.options.set(last, this.options.get(last).withTooltip(tooltip));
            return this;
        }

        /**
         * Item drawn on the left of the last added option's button. An item that resolves to air
         * (e.g. an unknown id) leaves the button without icon.
         */
        public Builder<T> icon(ItemLike icon) {
            int last = lastOption("icon");
            this.options.set(last, this.options.get(last).withIcon(icon));
            return this;
        }

        private int lastOption(String method) {
            if (this.options.isEmpty()) {
                throw new IllegalStateException(method + "() must follow an option()");
            }
            return this.options.size() - 1;
        }

        public Builder<T> description(Function<T, Component> description) {
            this.description = description;
            return this;
        }

        public Builder<T> required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder<T> initial(@Nullable T initial) {
            this.initial = initial;
            return this;
        }

        public Builder<T> onSelect(Consumer<T> onSelect) {
            this.onSelect = onSelect;
            return this;
        }

        public Builder<T> onCreate(Consumer<T> onCreate) {
            this.onCreate = onCreate;
            return this;
        }

        public LayoutElement build() {
            if (this.options.isEmpty()) {
                throw new IllegalStateException("ChoiceRow " + this.id + " has no option");
            }
            ChoiceRow<T> row = new ChoiceRow<>(this);
            LayoutElement layout = row.buildLayout();
            row.restoreInitial(this.initial);
            CreateWorldState.of(this.context.screen()).rows().add(row);
            return layout;
        }
    }
}
