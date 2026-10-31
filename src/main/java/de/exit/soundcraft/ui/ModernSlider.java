package de.exit.soundcraft.ui;

import java.util.function.Consumer;

public class ModernSlider extends Widget {

    private float value; // 0.0 to 1.0
    private final Consumer<Float> onChange;
    private boolean dragging = false;

    public ModernSlider(int x, int y, int w, int h, float initialValue, Consumer<Float> onChange) {
        super(x, y, w, h);
        this.value = Math.max(0f, Math.min(1f, initialValue));
        this.onChange = onChange;
    }

    public void setValue(float val) {
        if (!dragging) {
            this.value = Math.max(0f, Math.min(1f, val));
        }
    }

    public float getValue() {
        return value;
    }

    @Override
    public void render(UiCanvas c) {
        if (!visible) return;

        int trackH = 4;
        int trackY = y + (h - trackH) / 2;
        int fillW = Math.round(value * w);

        // Background Track
        c.fillRoundRect(x, trackY, w, trackH, 2, Theme.CONTROL);
        c.roundBorder(x, trackY, w, trackH, 2, Theme.BORDER);

        // Filled Active Track in #3D7B7D Accent
        if (fillW > 0) {
            c.fillRoundRect(x, trackY, fillW, trackH, 2, Theme.ACCENT);
        }

        // Thumb Knob
        int thumbW = 10;
        int thumbH = 12;
        int thumbX = Math.max(x, Math.min(x + w - thumbW, x + fillW - thumbW / 2));
        int thumbY = y + (h - thumbH) / 2;
        boolean hovered = c.hovered(x, y, w, h) || dragging;

        int thumbColor = hovered ? Theme.ACCENT_HOVER : Theme.ON_ACCENT;
        c.fillRoundRect(thumbX, thumbY, thumbW, thumbH, 2, thumbColor);
        c.roundBorder(thumbX, thumbY, thumbW, thumbH, 2, Theme.BORDER_STRONG);
    }

    private void updateFromMouse(double mx) {
        float newVal = (float) (mx - x) / (float) w;
        this.value = Math.max(0f, Math.min(1f, newVal));
        if (onChange != null) onChange.accept(this.value);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (visible && active && contains(mx, my) && button == 0) {
            dragging = true;
            updateFromMouse(mx);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button) {
        if (dragging && button == 0) {
            updateFromMouse(mx);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging && button == 0) {
            dragging = false;
            return true;
        }
        return false;
    }
}
