package de.exit.soundcraft.media;

/**
 * Immutable representation of the currently detected media track.
 */
public record TrackInfo(
        String title,
        String artist,
        String album,
        String appName,
        long durationMs,
        long positionMs,
        boolean isPlaying,
        byte[] coverBytes
) {
    public static final TrackInfo EMPTY = new TrackInfo("", "", "", "", 0, 0, false, null);

    public boolean hasTrack() {
        return title != null && !title.isBlank();
    }

    public String formatTime(long ms) {
        if (ms < 0) return "--:--";
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public String positionFormatted() {
        return formatTime(positionMs);
    }

    public String durationFormatted() {
        return formatTime(durationMs);
    }

    public float progress() {
        if (durationMs <= 0) return 0f;
        return Math.max(0f, Math.min(1f, (float) positionMs / (float) durationMs));
    }
}
