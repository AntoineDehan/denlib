// This file was made with Claude Code
package net.denlille.denlib.client.section;

import net.denlille.denlib.DenLib;
import net.denlille.denlib.client.choice.ChoiceRowRegistry;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sections added at the bottom of the World tab of the world creation screen. Registration is global
 * (call it once, e.g. during client setup); sections are built every time the tab is (re)built, sorted by
 * {@code order} then id, each spanning the tab's two columns.
 */
public final class CreateWorldSections {
    private static final Map<ResourceLocation, Entry> SECTIONS = new ConcurrentHashMap<>();

    private CreateWorldSections() {
    }

    public static void register(ResourceLocation id, int order, SectionFactory factory) {
        if (SECTIONS.put(id, new Entry(id, order, factory)) != null) {
            DenLib.LOGGER.warn("World tab section {} registered twice; the last registration wins", id);
        }
    }

    public static List<Entry> sorted() {
        List<Entry> entries = new ArrayList<>(SECTIONS.values());
        entries.sort(Comparator.comparingInt(Entry::order).thenComparing(entry -> entry.id().toString()));
        return entries;
    }

    /** Called by the World tab mixin once vanilla built its rows. */
    public static void buildInto(CreateWorldScreen screen, GridLayout.RowHelper rowHelper) {
        ChoiceRowRegistry.reset(screen);
        SectionContext context = new SectionContext(screen, screen.getUiState());
        for (Entry entry : sorted()) {
            try {
                LayoutElement element = entry.factory().create(context);
                if (element != null) {
                    rowHelper.addChild(element, 2);
                }
            } catch (RuntimeException e) {
                DenLib.LOGGER.warn("World tab section {} failed to build and was skipped", entry.id(), e);
            }
        }
    }

    public record Entry(ResourceLocation id, int order, SectionFactory factory) {
    }
}
