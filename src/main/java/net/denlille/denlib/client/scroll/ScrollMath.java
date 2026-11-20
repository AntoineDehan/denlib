// This file was made with Claude Code
package net.denlille.denlib.client.scroll;

/** Pure geometry of the tab scroll: bounds, scrollbar thumb, thumb dragging. */
public final class ScrollMath {
    /** Empty space kept above and below the content when scrolling. */
    public static final int MARGIN = 4;
    public static final int MIN_THUMB = 32;

    private ScrollMath() {
    }

    public static boolean overflows(int contentHeight, int viewportHeight) {
        return contentHeight > viewportHeight;
    }

    public static int maxScroll(int contentHeight, int viewportHeight) {
        if (!overflows(contentHeight, viewportHeight)) {
            return 0;
        }
        return Math.max(0, contentHeight + 2 * MARGIN - viewportHeight);
    }

    public static double clamp(double offset, int contentHeight, int viewportHeight) {
        return Math.max(0, Math.min(offset, maxScroll(contentHeight, viewportHeight)));
    }

    public static int thumbHeight(int contentHeight, int viewportHeight) {
        int height = viewportHeight * viewportHeight / (contentHeight + 2 * MARGIN);
        return Math.max(MIN_THUMB, Math.min(height, viewportHeight - 2 * MARGIN));
    }

    public static int thumbY(double offset, int contentHeight, int viewportHeight, int top) {
        int max = maxScroll(contentHeight, viewportHeight);
        if (max == 0) {
            return top;
        }
        int travel = viewportHeight - thumbHeight(contentHeight, viewportHeight);
        return top + (int) Math.round(clamp(offset, contentHeight, viewportHeight) / max * travel);
    }

    /** Offset after dragging the thumb by {@code dragDy} pixels from where it was at {@code startOffset}. */
    public static double offsetForThumbDrag(double startOffset, double dragDy, int contentHeight, int viewportHeight) {
        int travel = viewportHeight - thumbHeight(contentHeight, viewportHeight);
        if (travel <= 0) {
            return clamp(startOffset, contentHeight, viewportHeight);
        }
        double offset = startOffset + dragDy * maxScroll(contentHeight, viewportHeight) / travel;
        return clamp(offset, contentHeight, viewportHeight);
    }
}
