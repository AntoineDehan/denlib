// This file was made with Claude Code
package net.denlille.denlib.client.scroll;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScrollMathTest {
    @Test
    void noScrollWhenFits() {
        assertFalse(ScrollMath.overflows(200, 200));
        assertEquals(0, ScrollMath.maxScroll(200, 200));
        assertEquals(0, ScrollMath.maxScroll(150, 200));
    }

    @Test
    void maxScrollAddsMargins() {
        assertTrue(ScrollMath.overflows(300, 200));
        assertEquals(108, ScrollMath.maxScroll(300, 200));
    }

    @Test
    void clampAfterShrink() {
        assertEquals(58, ScrollMath.clamp(108, 250, 200));
        assertEquals(0, ScrollMath.clamp(-5, 300, 200));
        assertEquals(0, ScrollMath.clamp(40, 150, 200));
    }

    @Test
    void thumbBounds() {
        assertEquals(129, ScrollMath.thumbHeight(300, 200));
        assertEquals(40, ScrollMath.thumbY(0, 300, 200, 40));
        assertEquals(40 + 200 - 129, ScrollMath.thumbY(108, 300, 200, 40));
        assertEquals(32, ScrollMath.thumbHeight(5000, 200));
    }

    @Test
    void dragMapsTrackToContent() {
        assertEquals(108, ScrollMath.offsetForThumbDrag(0, 71, 300, 200), 1e-9);
        assertEquals(0, ScrollMath.offsetForThumbDrag(50, -500, 300, 200), 1e-9);
    }
}
