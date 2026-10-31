package de.exit.soundcraft.media;

import de.exit.soundcraft.lyrics.LyricsService;
import de.exit.soundcraft.media.platform.LinuxMediaProvider;
import de.exit.soundcraft.media.platform.WindowsMediaProvider;
import de.exit.soundcraft.ui.SoundCraftScreen;

import java.util.Locale;

public class MediaManager {

    private static volatile TrackInfo currentTrack = TrackInfo.EMPTY;
    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    public static void init() {
        if (IS_WINDOWS) {
            WindowsMediaProvider.start(MediaManager::updateTrack);
        } else {
            LinuxMediaProvider.start(MediaManager::updateTrack);
        }
    }

    private static void updateTrack(TrackInfo track) {
        currentTrack = track;
        SoundCraftScreen.currentTrack = track;

        // Auto-fetch synced lyrics from LRCLIB
        if (track.hasTrack()) {
            LyricsService.updateForTrack(track.title(), track.artist(), track.album(), track.durationMs());
        }
    }

    public static TrackInfo getCurrentTrack() {
        return currentTrack;
    }

    public static void previous() {
        if (!IS_WINDOWS) {
            LinuxMediaProvider.sendCommand("previous");
        }
    }

    public static void playPause() {
        if (!IS_WINDOWS) {
            LinuxMediaProvider.sendCommand("play-pause");
        }
    }

    public static void next() {
        if (!IS_WINDOWS) {
            LinuxMediaProvider.sendCommand("next");
        }
    }
}
