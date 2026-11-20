// This file was made with Claude Code
package net.denlille.denlib.client.section;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class SwappableSectionTest {
    private static final class FakeElement implements LayoutElement {
        private int x;
        private int y;
        private final int width;
        private final int height;

        FakeElement(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override public void setX(int x) { this.x = x; }
        @Override public void setY(int y) { this.y = y; }
        @Override public int getX() { return this.x; }
        @Override public int getY() { return this.y; }
        @Override public int getWidth() { return this.width; }
        @Override public int getHeight() { return this.height; }
        @Override public void visitWidgets(Consumer<AbstractWidget> consumer) { }
    }

    @Test
    void delegatesSizeAndPosition() {
        SwappableSection section = new SwappableSection();
        FakeElement inner = new FakeElement(310, 40);
        section.swap(inner);
        section.setPosition(12, 34);

        assertEquals(310, section.getWidth());
        assertEquals(40, section.getHeight());
        assertEquals(12, inner.getX());
        assertEquals(34, inner.getY());
    }

    @Test
    void swappedContentTakesTheCurrentPosition() {
        SwappableSection section = new SwappableSection();
        section.swap(new FakeElement(310, 40));
        section.setPosition(12, 34);

        FakeElement replacement = new FakeElement(310, 60);
        section.swap(replacement);

        assertEquals(12, replacement.getX());
        assertEquals(34, replacement.getY());
        assertEquals(60, section.getHeight());
    }

    @Test
    void emptySectionTakesNoSpace() {
        SwappableSection section = new SwappableSection();
        section.swap(null);

        assertEquals(0, section.getWidth());
        assertEquals(0, section.getHeight());
    }
}
