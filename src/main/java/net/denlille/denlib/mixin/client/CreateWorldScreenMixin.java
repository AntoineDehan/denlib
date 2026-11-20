// This file was made with Claude Code
package net.denlille.denlib.mixin.client;

import net.denlille.denlib.client.CreateWorldState;
import net.denlille.denlib.client.CreateWorldStateHolder;
import net.denlille.denlib.client.TabUtil;
import net.denlille.denlib.client.choice.ChoiceRow;
import net.denlille.denlib.client.scroll.TabScroll;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Holds denlib's per-screen state in a field of the screen itself (released with the screen), runs the
 * required-choice gate on "Create New World" (the button and the Enter key both call onCreate), and
 * draws the tab scrollbar. Only rows actually built on this screen count, so a section that failed to
 * inject never blocks creation.
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin implements CreateWorldStateHolder {
    @Unique
    private final CreateWorldState denlib$stateField = new CreateWorldState();

    @Override
    public CreateWorldState denlib$state() {
        return this.denlib$stateField;
    }

    @Inject(method = "onCreate", at = @At("HEAD"), cancellable = true, require = 0)
    private void denlib$requireChoices(CallbackInfo ci) {
        CreateWorldScreen self = (CreateWorldScreen) (Object) this;
        List<ChoiceRow<?>> rows = List.copyOf(this.denlib$stateField.rows());
        List<ChoiceRow<?>> missing = rows.stream().filter(row -> row.isRequired() && row.isMissing()).toList();
        if (!missing.isEmpty()) {
            missing.forEach(ChoiceRow::flagMissing);
            ChoiceRow<?> first = missing.get(0);
            TabUtil.selectTabContaining(self, first.title());
            TabScroll.reveal(first.title());
            ci.cancel();
            return;
        }
        rows.forEach(ChoiceRow::fireOnCreate);
    }

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void denlib$recordMouse(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TabScroll.recordMouse(mouseX, mouseY);
    }

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void denlib$renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TabScroll.renderScrollbar(graphics);
    }
}
