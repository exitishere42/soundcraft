package de.exit.soundcraft.ui;

public abstract class Widget {

    public int x;
    public int y;
    public int w;
    public int h;
    public boolean visible = true;
    public boolean active = true;

    public Widget(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public abstract void render(UiCanvas c);

    public boolean contains(double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) {
        return false;
    }

    public boolean mouseDragged(double mx, double my, int button) {
        return false;
    }

    public boolean focusable() {
        return false;
    }
}
