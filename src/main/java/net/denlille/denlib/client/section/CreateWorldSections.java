// This file was made with Claude Code
package net.denlille.denlib.client.section;

import net.denlille.denlib.DenLib;
import net.denlille.denlib.client.CreateWorldState;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sections added at the bottom of the World tab of the world creation screen. Registration is global
 * (call it once, e.g. during client setup); sections are built when the tab is built, sorted by
 * {@code order} then id, each spanning the tab's two columns, and rebuilt in place when the player
 * changes the data packs (so datapack-defined options stay accurate).
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
        CreateWorldState state = CreateWorldState.of(screen);
        WorldCreationUiState uiState = screen.getUiState();
        state.rows().clear();
        state.sections().clear();
        state.setDataPackResources(uiState.getSettings().dataPackResources());
        SectionContext context = new SectionContext(screen, uiState);
        for (Entry entry : sorted()) {
            SwappableSection holder = new SwappableSection();
            holder.swap(build(entry, context));
            state.sections().add(new CreateWorldState.BuiltSection(entry, holder));
            rowHelper.addChild(holder, 2);
        }
        uiState.addListener(changed -> onUiStateChanged(screen, changed));
    }

    /**
     * Data packs are only changed from the More tab (Data Packs / Experiments), so the World tab is not
     * the current tab here: its widgets are collected again when the player switches back to it.
     */
    private static void onUiStateChanged(CreateWorldScreen screen, WorldCreationUiState uiState) {
        CreateWorldState state = CreateWorldState.of(screen);
        Object resources = uiState.getSettings().dataPackResources();
        if (resources == state.dataPackResources()) {
            return;
        }
        state.setDataPackResources(resources);
        state.rows().clear();
        SectionContext context = new SectionContext(screen, uiState);
        for (CreateWorldState.BuiltSection section : state.sections()) {
            section.holder().swap(build(section.entry(), context));
        }
    }

    @Nullable
    private static LayoutElement build(Entry entry, SectionContext context) {
        try {
            return entry.factory().create(context);
        } catch (RuntimeException e) {
            DenLib.LOGGER.warn("World tab section {} failed to build and was skipped", entry.id(), e);
            return null;
        }
    }

    public record Entry(ResourceLocation id, int order, SectionFactory factory) {
    }
}
