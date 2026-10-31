package de.exit.soundcraft.ui;

/**
 * Color palette and metrics for SoundCraft, styled after OpenSoundboard
 * with the user-selected theme:
 * - Background: #1E1E24
 * - Primary/Accent: #3D7B7D
 * - Text/Secondary: #F4F4F6
 */
public final class Theme {

    private Theme() {
    }

    // ---- Metrics --------------------------------------------------------------------------
    public static final int PAD = 8;
    public static final int GAP = 4;
    public static final int HEADER_H = 24;
    public static final int RADIUS = 3;

    // ---- Colors (ARGB) --------------------------------------------------------------------
    public static final int ACCENT = 0xFF3D7B7D;
    public static final int ACCENT_HOVER = 0xFF4D9295;
    public static final int ACCENT_ACTIVE = 0xFF2F6365;
    public static final int ACCENT_SOFT = 0x303D7B7D;
    public static final int ON_ACCENT = 0xFFF4F4F6;

    public static final int SCRIM = 0x88000000;
    public static final int PANEL = 0xF01E1E24;
    public static final int HEADER = 0xFF18181D;
    public static final int SURFACE = 0xFF26262E;
    public static final int SURFACE_HOVER = 0xFF30303A;
    public static final int CONTROL = 0xFF2C2C35;
    public static final int CONTROL_HOVER = 0xFF383842;
    public static final int CONTROL_DISABLED = 0xFF202026;

    public static final int BORDER = 0xFF34343F;
    public static final int BORDER_STRONG = 0xFF424250;
    public static final int FIELD = 0xFF16161A;

    public static final int TEXT = 0xFFF4F4F6;
    public static final int TEXT_MUTED = 0xFF8E8E9C;
    public static final int TEXT_FAINT = 0xFF5C5C68;
    public static final int SELECTION = 0x603D7B7D;

    public static final int DANGER = 0xFFE05252;
    public static final int SUCCESS = 0xFF48B878;

    // ---- Color Math -----------------------------------------------------------------------

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    public static int mix(int a, int b, float t) {
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return (Math.round(aa + (ba - aa) * t) << 24)
                | (Math.round(ar + (br - ar) * t) << 16)
                | (Math.round(ag + (bg - ag) * t) << 8)
                | Math.round(ab + (bb - ab) * t);
    }

    public static float luminance(int rgb) {
        return (0.2126f * ((rgb >> 16) & 0xFF) + 0.7152f * ((rgb >> 8) & 0xFF) + 0.0722f * (rgb & 0xFF)) / 255f;
    }
}
