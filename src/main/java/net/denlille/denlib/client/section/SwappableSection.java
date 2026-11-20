// This file was made with Claude Code
package net.denlille.denlib.client.section;

import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * The slot a section occupies in the World tab. Its content can be rebuilt (e.g. after data packs
 * changed) without touching the tab's grid, which cannot remove children.
 */
public final class SwappableSection implements Layout {
    private int x;
    private int y;
    @Nullable
    private LayoutElement inner;

    public void swap(@Nullable LayoutElement content) {
        this.inner = content;
        if (content != null) {
            content.setX(this.x);
            content.setY(this.y);
        }
    }

    @Override
    public void setX(int x) {
        this.x = x;
        if (this.inner != null) {
            this.inner.setX(x);
        }
    }

    @Override
    public void setY(int y) {
        this.y = y;
        if (this.inner != null) {
            this.inner.setY(y);
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.inner == null ? 0 : this.inner.getWidth();
    }

    @Override
    public int getHeight() {
        return this.inner == null ? 0 : this.inner.getHeight();
    }

    /**
     * Being a Layout matters: a parent GridLayout only arranges children that are Layouts, so without it
     * the section's own grid would never place its widgets (they would all pile up at one point).
     */
    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        if (this.inner != null) {
            consumer.accept(this.inner);
        }
    }
}
