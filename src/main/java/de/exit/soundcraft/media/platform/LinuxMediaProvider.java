package de.exit.soundcraft.media.platform;

import de.exit.soundcraft.media.TrackInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Linux MPRIS (Media Player Remote Interfacing Specification) Provider.
 * Communicates with Linux desktop media players via D-Bus / playerctl.
 */
public class LinuxMediaProvider {

    private static volatile boolean running = false;

    public static void start(Consumer<TrackInfo> onUpdate) {
        if (running) return;
        running = true;

        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    // Check if playerctl exists
                    ProcessBuilder pb = new ProcessBuilder(
                            "playerctl", "metadata", "--format", "{{title}}|||{{artist}}|||{{album}}|||{{mpris:trackid}}|||{{status}}|||{{position}}|||{{mpris:length}}"
                    );
                    Process p = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
                    String line = reader.readLine();
                    p.waitFor();

                    if (line != null && line.contains("|||")) {
                        String[] parts = line.split("\\|\\|\\|", -1);
                        String title = parts.length > 0 ? parts[0] : "";
                        String artist = parts.length > 1 ? parts[1] : "";
                        String album = parts.length > 2 ? parts[2] : "";
                        boolean playing = parts.length > 4 && parts[4].equalsIgnoreCase("Playing");
                        long posUs = parts.length > 5 && !parts[5].isBlank() ? parseLongSafe(parts[5]) : 0;
                        long durUs = parts.length > 6 && !parts[6].isBlank() ? parseLongSafe(parts[6]) : 0;

                        TrackInfo info = new TrackInfo(title, artist, album, "Media Player", durUs / 1000, posUs / 1000, playing, null);
                        onUpdate.accept(info);
                    } else {
                        onUpdate.accept(TrackInfo.EMPTY);
                    }
                } catch (Exception ignored) {
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "SoundCraft-LinuxMediaWorker");
        thread.setDaemon(true);
        thread.start();
    }

    public static void sendCommand(String cmd) {
        try {
            new ProcessBuilder("playerctl", cmd).start();
        } catch (Exception ignored) {
        }
    }

    private static long parseLongSafe(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public static void stop() {
        running = false;
    }
}
