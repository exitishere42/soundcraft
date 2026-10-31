package de.exit.soundcraft.hud;

import de.exit.soundcraft.lyrics.LyricsService;
import de.exit.soundcraft.media.MediaManager;
import de.exit.soundcraft.media.TrackInfo;
import de.exit.soundcraft.ui.Theme;
import de.exit.soundcraft.ui.UiCanvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Renders floating synced lyrics in the sky / upper screen area.
 */
public class SkyLyricsRenderer {

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

        String currentLine = LyricsService.getCurrentLine(track.positionMs());
        if (currentLine.isBlank()) return;

        UiCanvas c = new UiCanvas(g, font, 0, 0);

        int centerX = screenW / 2;
        int currentY = 44; // Just below the Dynamic Island

        int textW = c.textWidth(currentLine);
        int padX = 10;
        int boxW = textW + padX * 2;
        int boxH = 16;
        int boxX = centerX - boxW / 2;

        // Subtle glowing backdrop for floating sky lyric (#1E1E24 with #3D7B7D border)
        c.fillRoundRect(boxX, currentY - 3, boxW, boxH, 3, 0xA01E1E24);
        c.roundBorder(boxX, currentY - 3, boxW, boxH, 3, Theme.ACCENT_SOFT);

        // Highlighted active lyric line in #F4F4F6
        c.centeredText(currentLine, centerX, currentY, Theme.TEXT);

        // Optional preview of next lyric line
        String nextLine = LyricsService.getNextLine(track.positionMs());
        if (!nextLine.isBlank()) {
            c.centeredText(nextLine, centerX, currentY + 15, Theme.TEXT_MUTED);
        }
    }
}
