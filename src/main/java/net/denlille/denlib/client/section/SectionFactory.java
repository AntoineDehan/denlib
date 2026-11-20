// This file was made with Claude Code
package net.denlille.denlib.client.section;

import net.minecraft.client.gui.layouts.LayoutElement;

import javax.annotation.Nullable;

/** Builds one section of the World tab; return {@code null} to add nothing for this screen. */
@FunctionalInterface
public interface SectionFactory {
    @Nullable
    LayoutElement create(SectionContext context);
}
