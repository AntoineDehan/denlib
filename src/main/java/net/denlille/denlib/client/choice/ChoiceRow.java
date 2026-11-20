// This file was made with Claude Code
package net.denlille.denlib.client.choice;

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
        return SelectionStore.get(screen, id);
    }

    public ResourceLocation id() {
        return this.id;
    }

    public StringWidget title() {
        return this.title;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isMissing() {
        return this.selected == null;
    }

    public void flagMissing() {
        this.title.setColor(COLOR_MISSING);
    }

    public void fireOnCreate() {
        if (this.onCreate != null && this.selected != null) {
            this.onCreate.accept(this.selected);
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
                Button button = Button.builder(option.label(), b -> select(option.value(), true))
                        .width(BUTTON_WIDTH)
                        .build();
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
        T start = SelectionStore.<T>get(this.screen, this.id)
                .filter(this::isOption)
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
        SelectionStore.put(this.screen, this.id, value);
        for (int i = 0; i < this.options.size(); i++) {
            this.buttons.get(i).active = !Objects.equals(this.options.get(i).value(), value);
        }
        this.title.setColor(COLOR_TITLE);
        if (this.descriptionWidget != null && this.description != null) {
            this.descriptionWidget.setMessage(this.description.apply(value));
        }
        if (byPlayer) {
            if (this.onSelect != null) {
                this.onSelect.accept(value);
            }
            // The description height may have changed: re-run the layout (and the tab scroll bounds).
            this.screen.repositionElements();
        }
    }

    private record Option<T>(T value, Component label, @Nullable Component tooltip) {
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
            this.options.add(new Option<>(value, label, null));
            return this;
        }

        /** Tooltip of the last added option. */
        public Builder<T> tooltip(Component tooltip) {
            if (this.options.isEmpty()) {
                throw new IllegalStateException("tooltip() must follow an option()");
            }
            Option<T> last = this.options.remove(this.options.size() - 1);
            this.options.add(new Option<>(last.value(), last.label(), tooltip));
            return this;
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
            ChoiceRowRegistry.add(this.context.screen(), row);
            return layout;
        }
    }
}
