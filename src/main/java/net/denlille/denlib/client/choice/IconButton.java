// This file was made with Claude Code
package net.denlille.denlib.client.choice;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/** A vanilla button with an item drawn on its left; the label is centered in the remaining space. */
@ApiStatus.Internal
public final class IconButton extends Button {
    /** Item (16 px) plus its margins. */
    private static final int ICON_SPACE = 20;

    private final ItemStack icon;

    public IconButton(int width, Component message, ItemStack icon, OnPress onPress) {
        super(0, 0, width, 20, message, onPress, DEFAULT_NARRATION);
        this.icon = icon;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.renderFakeItem(this.icon, getX() + 3, getY() + (getHeight() - 16) / 2);
    }

    @Override
    public void renderString(GuiGraphics graphics, Font font, int color) {
        renderScrollingString(graphics, font, getMessage(), getX() + ICON_SPACE, getY(),
                getX() + getWidth() - 2, getY() + getHeight(), color);
    }
}
