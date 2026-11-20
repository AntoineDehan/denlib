// This file was made with Claude Code
package net.denlille.denlib.client.section;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;

/** What a section factory gets to build its section: the creation screen being built. */
public record SectionContext(CreateWorldScreen screen, WorldCreationUiState uiState) {
}
