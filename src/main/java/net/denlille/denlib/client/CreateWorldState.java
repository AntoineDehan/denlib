// This file was made with Claude Code
package net.denlille.denlib.client;

import net.denlille.denlib.client.choice.ChoiceRow;
import net.denlille.denlib.client.section.CreateWorldSections;
import net.denlille.denlib.client.section.SwappableSection;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Everything denlib keeps about one world creation screen. It lives in a field added to the screen
 * itself (no static map), so it is released with the screen when the player creates or cancels.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class CreateWorldState {
    private final Map<ResourceLocation, Object> selections = new HashMap<>();
    private final List<ChoiceRow<?>> rows = new ArrayList<>();
    private final List<BuiltSection> sections = new ArrayList<>();
    @Nullable
    private Object dataPackResources;

    /** The screen's state, or a throwaway one for anything that is not a creation screen. */
    public static CreateWorldState of(Object screen) {
        return screen instanceof CreateWorldStateHolder holder ? holder.denlib$state() : new CreateWorldState();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> selection(ResourceLocation rowId) {
        return Optional.ofNullable((T) this.selections.get(rowId));
    }

    /** The selection if it is still valid; an invalid one (e.g. its datapack was removed) is forgotten. */
    public <T> Optional<T> validSelection(ResourceLocation rowId, Predicate<T> isValid) {
        Optional<T> selection = selection(rowId);
        if (selection.isPresent() && !isValid.test(selection.get())) {
            this.selections.remove(rowId);
            return Optional.empty();
        }
        return selection;
    }

    public void select(ResourceLocation rowId, Object value) {
        this.selections.put(rowId, value);
    }

    public List<ChoiceRow<?>> rows() {
        return this.rows;
    }

    public List<BuiltSection> sections() {
        return this.sections;
    }

    @Nullable
    public Object dataPackResources() {
        return this.dataPackResources;
    }

    public void setDataPackResources(@Nullable Object dataPackResources) {
        this.dataPackResources = dataPackResources;
    }

    /** A registered section and the holder it was placed in, so it can be rebuilt in place. */
    public record BuiltSection(CreateWorldSections.Entry entry, SwappableSection holder) {
    }
}
