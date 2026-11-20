// This file was made with Claude Code
package net.denlille.denlib.client.scroll;

import net.denlille.denlib.DenLib;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mouse input of the tab scroll: wheel, scrollbar click and drag. */
@Mod.EventBusSubscriber(modid = DenLib.MODID, value = Dist.CLIENT)
public final class TabScrollEvents {
    private TabScrollEvents() {
    }

    /** Post: only fires when no widget consumed the wheel. */
    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Post event) {
        if (event.getScreen() instanceof CreateWorldScreen) {
            TabScroll.mouseScrolled(event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof CreateWorldScreen
                && TabScroll.mousePressed(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (event.getScreen() instanceof CreateWorldScreen && TabScroll.mouseDragged(event.getMouseY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        TabScroll.mouseReleased();
    }
}
