package de.exit.soundcraft.ui;

public class ButtonWidget extends Widget {

    private final String label;
    private final Icons icon;
    private final Runnable onClick;
    private boolean primary;

    public ButtonWidget(int x, int y, int w, int h, String label, Icons icon, Runnable onClick) {
        super(x, y, w, h);
        this.label = label;
        this.icon = icon;
        this.onClick = onClick;
        this.primary = false;
    }

    public ButtonWidget setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }

    @Override
    public void render(UiCanvas c) {
        if (!visible) return;

        boolean hovered = c.hovered(x, y, w, h) && active;
        int bg = primary
                ? (hovered ? Theme.ACCENT_HOVER : Theme.ACCENT)
                : (hovered ? Theme.CONTROL_HOVER : Theme.CONTROL);
        int borderCol = primary ? Theme.ACCENT_HOVER : Theme.BORDER;
        int textCol = primary ? Theme.ON_ACCENT : (hovered ? Theme.TEXT : Theme.TEXT_MUTED);

        if (!active) {
            bg = Theme.CONTROL_DISABLED;
            textCol = Theme.TEXT_FAINT;
            borderCol = Theme.BORDER;
        }

        c.fillRoundRect(x, y, w, h, bg);
        c.roundBorder(x, y, w, h, borderCol);

        if (icon != null && label != null && !label.isBlank()) {
            int totalContentW = icon.width + 4 + c.textWidth(label);
            int startX = x + (w - totalContentW) / 2;
            int iconY = y + (h - icon.height) / 2;
            c.icon(icon, startX, iconY, textCol);
            c.text(label, startX + icon.width + 4, y + (h - c.lineHeight()) / 2 + 1, textCol);
        } else if (icon != null) {
            c.icon(icon, x, y, w, h, textCol);
        } else if (label != null) {
            c.centeredText(label, x + w / 2, y + (h - c.lineHeight()) / 2 + 1, textCol);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (visible && active && contains(mx, my) && button == 0) {
            if (onClick != null) onClick.run();
            return true;
        }
        return false;
    }
}
