// This file was made with Claude Code
package net.denlille.denlib.client.choice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** The choice rows currently built on each creation screen (reset every time the World tab is rebuilt). */
public final class ChoiceRowRegistry {
    private static final Map<Object, List<ChoiceRow<?>>> LIVE = new WeakHashMap<>();

    private ChoiceRowRegistry() {
    }

    public static void reset(Object screen) {
        LIVE.remove(screen);
    }

    static void add(Object screen, ChoiceRow<?> row) {
        LIVE.computeIfAbsent(screen, s -> new ArrayList<>()).add(row);
    }

    public static List<ChoiceRow<?>> live(Object screen) {
        return List.copyOf(LIVE.getOrDefault(screen, List.of()));
    }
}
