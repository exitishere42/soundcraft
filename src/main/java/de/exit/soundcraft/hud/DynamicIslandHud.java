package de.exit.soundcraft.hud;

import de.exit.soundcraft.media.MediaManager;
import de.exit.soundcraft.media.TrackInfo;
import de.exit.soundcraft.ui.Icons;
import de.exit.soundcraft.ui.Theme;
import de.exit.soundcraft.ui.UiCanvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DynamicIslandHud {

    private static boolean enabled = true;

    public static void setEnabled(boolean val) {
        enabled = val;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void render(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font, int screenW, int screenH) {
        if (!enabled) return;

        TrackInfo track = MediaManager.getCurrentTrack();
        if (!track.hasTrack() || !track.isPlaying()) return;

        UiCanvas c = new UiCanvas(g, font, 0, 0);

        int hudW = 180;
        int hudH = 26;
        int hudX = (screenW - hudW) / 2;
        int hudY = 6;

        // Dynamic Island Surface (#1E1E24 with 4px border & #34343F outline)
        c.fillRoundRect(hudX, hudY, hudW, hudH, 4, Theme.PANEL);
        c.roundBorder(hudX, hudY, hudW, hudH, 4, Theme.BORDER);

        // Music Note Icon / Indicator
        int iconSize = 16;
        int iconX = hudX + 5;
        int iconY = hudY + (hudH - iconSize) / 2;
        c.fillRoundRect(iconX, iconY, iconSize, iconSize, 2, Theme.SURFACE);
        c.icon(Icons.NOTE, iconX + (iconSize - Icons.NOTE.width) / 2, iconY + (iconSize - Icons.NOTE.height) / 2, Theme.ACCENT);

        // Song Title (#F4F4F6) & Artist (#3D7B7D)
        int textX = iconX + iconSize + 6;
        int textMaxW = hudW - iconSize - 16;

        String title = c.trimText(track.title(), textMaxW);
        c.text(title, textX, hudY + 4, Theme.TEXT);

        String artist = c.trimText(track.artist(), textMaxW);
        c.text(artist, textX, hudY + 14, Theme.ACCENT_HOVER);

        // 2px Progress Bar at bottom in Accent #3D7B7D
        int progressW = Math.round(track.progress() * (hudW - 8));
        if (progressW > 0) {
            c.fillRect(hudX + 4, hudY + hudH - 2, progressW, 2, Theme.ACCENT);
        }
    }
}
