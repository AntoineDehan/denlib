// This file was made with Claude Code
package net.denlille.denlib.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Finds tabs through the screen's public children (tab bar, then tab buttons, then tab) instead of
 * shadowing private fields, whose refmap entries the MixinGradle AP may fail to emit.
 */
@ApiStatus.Internal
public final class TabUtil {
    private TabUtil() {
    }

    /** Selects the tab that contains {@code widget}; returns false if no such tab was found. */
    public static boolean selectTabContaining(Screen screen, AbstractWidget widget) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof TabNavigationBar bar) {
                List<? extends GuiEventListener> buttons = bar.children();
                for (int i = 0; i < buttons.size(); i++) {
                    if (buttons.get(i) instanceof TabButton button && contains(button, widget)) {
                        bar.selectTab(i, false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** The tab currently shown on {@code screen}, or null if the screen has no tab bar. */
    @Nullable
    public static Tab currentTab(Screen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof TabNavigationBar bar) {
                for (GuiEventListener button : bar.children()) {
                    if (button instanceof TabButton tabButton && tabButton.isSelected()) {
                        return tabButton.tab();
                    }
                }
            }
        }
        return null;
    }

    private static boolean contains(TabButton button, AbstractWidget widget) {
        boolean[] found = {false};
        button.tab().visitChildren(child -> {
            if (child == widget) {
                found[0] = true;
            }
        });
        return found[0];
    }
}
