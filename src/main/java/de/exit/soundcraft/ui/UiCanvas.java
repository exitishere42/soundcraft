package de.exit.soundcraft.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * High-performance drawing canvas adapted from OpenSoundboard
 * for Minecraft 26.3 with GuiGraphicsExtractor.
 */
public final class UiCanvas {

    public final GuiGraphicsExtractor g;
    public final Font font;
    public final int mouseX;
    public final int mouseY;

    public UiCanvas(GuiGraphicsExtractor g, Font font, int mouseX, int mouseY) {
        this.g = g;
        this.font = font;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    // ---- Shapes ---------------------------------------------------------------------------

    public void fillRect(int x, int y, int w, int h, int argb) {
        if (w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, argb);
    }

    public void border(int x, int y, int w, int h, int argb) {
        fillRect(x, y, w, 1, argb);
        fillRect(x, y + h - 1, w, 1, argb);
        fillRect(x, y + 1, 1, h - 2, argb);
        fillRect(x + w - 1, y + 1, 1, h - 2, argb);
    }

    public void fillRoundRect(int x, int y, int w, int h, int r, int argb) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            fillRect(x, y, w, h, argb);
            return;
        }
        fillRect(x, y + r, w, h - r * 2, argb);
        for (int i = 0; i < r; i++) {
            int inset = r - i;
            fillRect(x + inset, y + i, w - inset * 2, 1, argb);
            fillRect(x + inset, y + h - 1 - i, w - inset * 2, 1, argb);
        }
    }

    public void fillRoundRect(int x, int y, int w, int h, int argb) {
        fillRoundRect(x, y, w, h, Theme.RADIUS, argb);
    }

    public void roundBorder(int x, int y, int w, int h, int r, int argb) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            border(x, y, w, h, argb);
            return;
        }
        fillRect(x + r, y, w - r * 2, 1, argb);
        fillRect(x + r, y + h - 1, w - r * 2, 1, argb);
        fillRect(x, y + r, 1, h - r * 2, argb);
        fillRect(x + w - 1, y + r, 1, h - r * 2, argb);
        for (int i = 1; i < r; i++) {
            int inset = r - i;
            fillRect(x + inset, y + i, 1, 1, argb);
            fillRect(x + w - 1 - inset, y + i, 1, 1, argb);
            fillRect(x + inset, y + h - 1 - i, 1, 1, argb);
            fillRect(x + w - 1 - inset, y + h - 1 - i, 1, 1, argb);
        }
    }

    public void roundBorder(int x, int y, int w, int h, int argb) {
        roundBorder(x, y, w, h, Theme.RADIUS, argb);
    }

    // ---- Text -----------------------------------------------------------------------------

    public void text(String s, int x, int y, int color) {
        g.text(font, s, x, y, color, false);
    }

    public void centeredText(String s, int centerX, int y, int color) {
        text(s, centerX - textWidth(s) / 2, y, color);
    }

    public void rightText(String s, int rightX, int y, int color) {
        text(s, rightX - textWidth(s), y, color);
    }

    public int textWidth(String s) {
        return font.width(s);
    }

    public int lineHeight() {
        return font.lineHeight;
    }

    public String trimText(String s, int maxWidth) {
        if (textWidth(s) <= maxWidth) return s;
        String ellipsis = "...";
        int contentWidth = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(s, contentWidth) + ellipsis;
    }

    // ---- Icons ----------------------------------------------------------------------------

    public void icon(Icons icon, int x, int y, int color) {
        icon.draw(this, x, y, color);
    }

    public void icon(Icons icon, int x, int y, int w, int h, int color) {
        icon(icon, x + (w - icon.width) / 2, y + (h - icon.height) / 2, color);
    }

    // ---- Scissor & Hover ------------------------------------------------------------------

    public void pushScissor(int x, int y, int w, int h) {
        g.enableScissor(x, y, x + w, y + h);
    }

    public void popScissor() {
        g.disableScissor();
    }

    public boolean hovered(int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
