// This file was made with Claude Code
package net.denlille.denlib.client.scroll;

import net.denlille.denlib.client.TabUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Automatic scroll of overflowing creation tabs. Widgets stay regular children of the screen (other
 * mods can still find them); this class only moves the tab layout, and the widget mixin clips their
 * rendering to the viewport and ignores the mouse outside of it.
 */
@ApiStatus.Internal
public final class TabScroll {
    public static final int SCROLL_STEP = 20;
    public static final int BAR_WIDTH = 6;
    public static final int BAR_GAP = 6;
    /** Vanilla footer: 36 px high, its separator drawn 2 px above. */
    private static final int FOOTER_HEIGHT = 36;

    private static final Map<Tab, Double> OFFSETS = new WeakHashMap<>();
    @Nullable
    private static Active active;
    private static double mouseX = -1;
    private static double mouseY = -1;
    private static boolean dragging;
    private static double dragStartMouseY;
    private static double dragStartOffset;

    private TabScroll() {
    }

    /** Called after a GridLayoutTab laid itself out in {@code area} (the current tab of its screen). */
    public static void afterLayout(Tab tab, GridLayout layout, ScreenRectangle area) {
        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof CreateWorldScreen)) {
            return;
        }
        int footerTop = Mth.roundToward(screen.height - FOOTER_HEIGHT - 2, 2);
        int viewportHeight = Math.min(area.bottom(), footerTop) - area.top();
        ScreenRectangle viewport = new ScreenRectangle(area.left(), area.top(), area.width(), viewportHeight);
        int contentHeight = layout.getHeight();
        if (!ScrollMath.overflows(contentHeight, viewportHeight)) {
            active = null;
            dragging = false;
            return;
        }
        Set<AbstractWidget> widgets = Collections.newSetFromMap(new IdentityHashMap<>());
        tab.visitChildren(widgets::add);
        active = new Active(screen, tab, layout, viewport, contentHeight, widgets);
        scrollTo(OFFSETS.getOrDefault(tab, 0.0));
    }

    /** Scrolls the current tab so that {@code widget} is fully visible. */
    public static void reveal(AbstractWidget widget) {
        Active current = current();
        if (current == null || !current.widgets().contains(widget)) {
            return;
        }
        ScreenRectangle viewport = current.viewport();
        int visibleTop = viewport.top() + ScrollMath.MARGIN;
        int visibleBottom = viewport.bottom() - ScrollMath.MARGIN;
        double offset = offset(current);
        if (widget.getY() < visibleTop) {
            offset -= visibleTop - widget.getY();
        } else if (widget.getY() + widget.getHeight() > visibleBottom) {
            offset += widget.getY() + widget.getHeight() - visibleBottom;
        }
        scrollTo(offset);
    }

    /** True if {@code widget} belongs to the scrolled tab currently shown. */
    public static boolean isClipped(AbstractWidget widget) {
        Active current = current();
        return current != null && current.widgets().contains(widget);
    }

    @Nullable
    public static ScreenRectangle area() {
        Active current = current();
        return current == null ? null : current.viewport();
    }

    public static boolean isInArea(double x, double y) {
        Active current = current();
        return current != null && contains(current.viewport(), x, y);
    }

    /** Mouse position of the frame being rendered, recorded by the creation screen. */
    public static void recordMouse(double x, double y) {
        mouseX = x;
        mouseY = y;
        dropIfTabChanged();
    }

    /**
     * The scroll state only follows GridLayoutTabs (their doLayout re-arms it). If the player switched
     * to a mod tab that is not one, the previous tab's state must not keep drawing its scrollbar or
     * swallowing clicks. Checked once per frame and before handling mouse input.
     */
    private static void dropIfTabChanged() {
        Active current = current();
        if (current != null) {
            Tab shown = TabUtil.currentTab(current.screen());
            if (shown != null && shown != current.tab()) {
                active = null;
                dragging = false;
            }
        }
    }

    public static boolean isMouseInArea() {
        return isInArea(mouseX, mouseY);
    }

    public static boolean mouseScrolled(double x, double y, double delta) {
        dropIfTabChanged();
        Active current = current();
        if (current == null || !contains(current.viewport(), x, y)) {
            return false;
        }
        scrollTo(offset(current) - delta * SCROLL_STEP);
        return true;
    }

    public static boolean mousePressed(double x, double y, int button) {
        dropIfTabChanged();
        Active current = current();
        if (current == null || button != 0 || !contains(bar(current), x, y)) {
            return false;
        }
        int thumbTop = thumbY(current);
        int thumbHeight = ScrollMath.thumbHeight(current.contentHeight(), current.viewport().height());
        if (y < thumbTop) {
            scrollTo(offset(current) - current.viewport().height());
        } else if (y >= thumbTop + thumbHeight) {
            scrollTo(offset(current) + current.viewport().height());
        } else {
            dragging = true;
            dragStartMouseY = y;
            dragStartOffset = offset(current);
        }
        return true;
    }

    public static boolean mouseDragged(double y) {
        Active current = current();
        if (current == null || !dragging) {
            return false;
        }
        scrollTo(ScrollMath.offsetForThumbDrag(dragStartOffset, y - dragStartMouseY,
                current.contentHeight(), current.viewport().height()));
        return true;
    }

    public static void mouseReleased() {
        dragging = false;
    }

    /** The screen is closing: release everything that references it. */
    public static void forget(Screen screen) {
        Active current = active;
        if (current != null && current.screen() == screen) {
            active = null;
            dragging = false;
        }
    }

    /** Draws the scrollbar, styled like the vanilla selection lists. */
    public static void renderScrollbar(GuiGraphics graphics) {
        Active current = current();
        if (current == null) {
            return;
        }
        ScreenRectangle bar = bar(current);
        int thumbTop = thumbY(current);
        int thumbHeight = ScrollMath.thumbHeight(current.contentHeight(), current.viewport().height());
        graphics.fill(bar.left(), bar.top(), bar.right(), bar.bottom(), 0xFF000000);
        graphics.fill(bar.left(), thumbTop, bar.right(), thumbTop + thumbHeight, 0xFF808080);
        graphics.fill(bar.left(), thumbTop, bar.right() - 1, thumbTop + thumbHeight - 1, 0xFFC0C0C0);
    }

    @Nullable
    private static Active current() {
        Active current = active;
        if (current != null && current.screen() != Minecraft.getInstance().screen) {
            active = null;
            dragging = false;
            return null;
        }
        return current;
    }

    private static double offset(Active current) {
        return OFFSETS.getOrDefault(current.tab(), 0.0);
    }

    private static void scrollTo(double offset) {
        Active current = current();
        if (current == null) {
            return;
        }
        double clamped = ScrollMath.clamp(offset, current.contentHeight(), current.viewport().height());
        OFFSETS.put(current.tab(), clamped);
        current.layout().setY(current.viewport().top() + ScrollMath.MARGIN - (int) Math.round(clamped));
    }

    private static int thumbY(Active current) {
        return ScrollMath.thumbY(offset(current), current.contentHeight(), current.viewport().height(),
                current.viewport().top());
    }

    /** Just right of the content, like the vanilla lists, but always inside the viewport. */
    private static ScreenRectangle bar(Active current) {
        ScreenRectangle viewport = current.viewport();
        int x = Math.min(current.layout().getX() + current.layout().getWidth() + BAR_GAP, viewport.right() - BAR_WIDTH);
        return new ScreenRectangle(x, viewport.top(), BAR_WIDTH, viewport.height());
    }

    private static boolean contains(ScreenRectangle rect, double x, double y) {
        return x >= rect.left() && x < rect.right() && y >= rect.top() && y < rect.bottom();
    }

    private record Active(Screen screen, Tab tab, GridLayout layout, ScreenRectangle viewport,
                          int contentHeight, Set<AbstractWidget> widgets) {
    }
}
