// This file was made with Claude Code
package net.denlille.denlib.mixin.client;

import net.denlille.denlib.client.scroll.TabScroll;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-positions the tab content when it overflows its area (see TabScroll). The layout is taken from
 * the alignInRectangle call rather than a @Shadow of the protected field: the MixinGradle AP does not
 * emit refmap entries for that field, which would break in the obfuscated production runtime.
 */
@Mixin(GridLayoutTab.class)
public abstract class GridLayoutTabMixin {
    @Unique
    private GridLayout denlib$layout;

    @ModifyArg(method = "doLayout",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/FrameLayout;alignInRectangle(Lnet/minecraft/client/gui/layouts/LayoutElement;Lnet/minecraft/client/gui/navigation/ScreenRectangle;FF)V"),
            index = 0, require = 0)
    private LayoutElement denlib$captureLayout(LayoutElement element) {
        if (element instanceof GridLayout grid) {
            this.denlib$layout = grid;
        }
        return element;
    }

    @Inject(method = "doLayout", at = @At("TAIL"), require = 0)
    private void denlib$scrollIfOverflowing(ScreenRectangle area, CallbackInfo ci) {
        if (this.denlib$layout != null) {
            TabScroll.afterLayout((Tab) this, this.denlib$layout, area);
        }
    }
}
