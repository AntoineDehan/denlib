// This file was made with Claude Code
package net.denlille.denlib.client.choice;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Choice-row selections, kept per screen instance: they survive the tab rebuild that a window
 * resize triggers, and vanish with the screen when it is closed (no leak into the next world).
 */
public final class SelectionStore {
    private static final Map<Object, Map<ResourceLocation, Object>> SELECTIONS = new WeakHashMap<>();

    private SelectionStore() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> get(Object screen, ResourceLocation rowId) {
        Map<ResourceLocation, Object> rows = SELECTIONS.get(screen);
        return rows == null ? Optional.empty() : Optional.ofNullable((T) rows.get(rowId));
    }

    public static void put(Object screen, ResourceLocation rowId, Object value) {
        SELECTIONS.computeIfAbsent(screen, s -> new HashMap<>()).put(rowId, value);
    }
}
