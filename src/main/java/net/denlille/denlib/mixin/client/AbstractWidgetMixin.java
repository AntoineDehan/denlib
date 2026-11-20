// This file was made with Claude Code
package net.denlille.denlib.mixin.client;

import net.denlille.denlib.client.scroll.TabScroll;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Widgets of a scrolled creation tab: rendering clipped to the tab viewport, no hover nor click while
 * the mouse is outside of it (e.g. a button scrolled under the footer). No-op for every other widget.
 */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {
    @Unique
    private boolean denlib$scissored;

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void denlib$clipStart(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractWidget self = (AbstractWidget) (Object) this;
        ScreenRectangle area = TabScroll.isClipped(self) ? TabScroll.area() : null;
        if (area != null) {
            graphics.enableScissor(area.left(), area.top(), area.right(), area.bottom());
            this.denlib$scissored = true;
        }
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 0)
    private int denlib$hideMouseX(int mouseX) {
        return denlib$mouseHidden() ? -1 : mouseX;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 1, argsOnly = true, require = 0)
    private int denlib$hideMouseY(int mouseY) {
        return denlib$mouseHidden() ? -1 : mouseY;
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void denlib$clipEnd(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.denlib$scissored) {
            graphics.disableScissor();
            this.denlib$scissored = false;
        }
    }

    @Inject(method = "isMouseOver", at = @At("RETURN"), cancellable = true, require = 0)
    private void denlib$isMouseOver(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && denlib$outside(mouseX, mouseY)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "clicked", at = @At("RETURN"), cancellable = true, require = 0)
    private void denlib$clicked(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && denlib$outside(mouseX, mouseY)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean denlib$mouseHidden() {
        return TabScroll.isClipped((AbstractWidget) (Object) this) && !TabScroll.isMouseInArea();
    }

    @Unique
    private boolean denlib$outside(double mouseX, double mouseY) {
        return TabScroll.isClipped((AbstractWidget) (Object) this) && !TabScroll.isInArea(mouseX, mouseY);
    }
}
