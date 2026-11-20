// This file was made with Claude Code
package net.denlille.denlib.mixin.client;

import net.denlille.denlib.DenLib;
import net.denlille.denlib.client.section.CreateWorldSections;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Appends the registered sections to the World tab. The tab's two-column RowHelper is the first
 * RowHelper local stored by the constructor; capturing it by STORE avoids depending on the full local
 * variable table. require = 0: if another mod reshapes the constructor, no section is added (and the
 * creation gate never blocks, since no choice row is present).
 */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class WorldTabMixin {
    @Unique
    private GridLayout.RowHelper denlib$rowHelper;

    @ModifyVariable(method = "<init>", at = @At("STORE"), ordinal = 0, require = 0)
    private GridLayout.RowHelper denlib$captureRowHelper(GridLayout.RowHelper rowHelper) {
        if (this.denlib$rowHelper == null) {
            this.denlib$rowHelper = rowHelper;
        }
        return rowHelper;
    }

    @Inject(method = "<init>", at = @At("TAIL"), require = 0)
    private void denlib$addSections(CreateWorldScreen screen, CallbackInfo ci) {
        if (this.denlib$rowHelper == null) {
            DenLib.LOGGER.warn("World tab layout not found; denlib sections were not added");
            return;
        }
        CreateWorldSections.buildInto(screen, this.denlib$rowHelper);
    }
}
