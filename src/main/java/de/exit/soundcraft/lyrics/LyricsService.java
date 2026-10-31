package de.exit.soundcraft.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Live synchronized lyrics service using LRCLIB (free & open-source).
 */
public class LyricsService {

    private static final String BASE_URL = "https://lrclib.net/api";
    private static final Pattern LRC_PATTERN = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2}(?:[.:]\\d{1,3})?)]");
    private static final Map<String, List<LyricLine>> CACHE = new ConcurrentHashMap<>();

    private static volatile String currentTrackId = "";
    private static volatile List<LyricLine> currentLyrics = Collections.emptyList();

    public static void updateForTrack(String title, String artist, String album, long durationMs) {
        if (title == null || title.isBlank() || artist == null || artist.isBlank()) {
            currentLyrics = Collections.emptyList();
            currentTrackId = "";
            return;
        }

        String trackId = artist + " - " + title;
        if (trackId.equals(currentTrackId)) return;
        currentTrackId = trackId;

        List<LyricLine> cached = CACHE.get(trackId);
        if (cached != null) {
            currentLyrics = cached;
            return;
        }

        // Fetch asynchronously in daemon thread
        Thread fetcher = new Thread(() -> {
            try {
                List<LyricLine> fetched = fetch(title, artist, album, durationMs);
                if (fetched != null && !fetched.isEmpty()) {
                    CACHE.put(trackId, fetched);
                    if (trackId.equals(currentTrackId)) {
                        currentLyrics = fetched;
                    }
                }
            } catch (Exception ignored) {
            }
        }, "SoundCraft-LyricsFetcher");
        fetcher.setDaemon(true);
        fetcher.start();
    }

    public static String getCurrentLine(long positionMs) {
        if (currentLyrics.isEmpty()) return "";

        String current = "";
        for (LyricLine line : currentLyrics) {
            if (line.timeMs() <= positionMs) {
                current = line.text();
            } else {
                break;
            }
        }
        return current;
    }

    public static String getNextLine(long positionMs) {
        if (currentLyrics.isEmpty()) return "";

        for (LyricLine line : currentLyrics) {
            if (line.timeMs() > positionMs) {
                return line.text();
            }
        }
        return "";
    }

    private static List<LyricLine> fetch(String title, String artist, String album, long durationMs) throws Exception {
        String query = "?track_name=" + enc(title) + "&artist_name=" + enc(artist);
        if (album != null && !album.isBlank()) query += "&album_name=" + enc(album);
        if (durationMs > 0) query += "&duration=" + Math.round(durationMs / 1000.0);

        String json = httpGet(BASE_URL + "/get" + query);
        String synced = null;

        if (json != null) {
            try {
                JsonObject o = JsonParser.parseString(json).getAsJsonObject();
                if (o.has("syncedLyrics") && !o.get("syncedLyrics").isJsonNull()) {
                    synced = o.get("syncedLyrics").getAsString();
                }
            } catch (Exception ignored) {
            }
        }

        // Search Fallback if direct match was not found
        if (synced == null) {
            String cleanTitle = title.replaceAll("\\s*[-\u2013]\\s.*$", "").replaceAll("\\s*[(\\[].*?[)\\]]", "").trim();
            String searchJson = httpGet(BASE_URL + "/search?track_name=" + enc(cleanTitle) + "&artist_name=" + enc(artist));
            if (searchJson != null) {
                try {
                    JsonArray arr = JsonParser.parseString(searchJson).getAsJsonArray();
                    for (JsonElement el : arr) {
                        JsonObject obj = el.getAsJsonObject();
                        if (obj.has("syncedLyrics") && !obj.get("syncedLyrics").isJsonNull()) {
                            synced = obj.get("syncedLyrics").getAsString();
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return synced != null ? parse(synced) : Collections.emptyList();
    }

    private static List<LyricLine> parse(String lrc) {
        List<LyricLine> lines = new ArrayList<>();
        for (String raw : lrc.split("\\r?\\n")) {
            Matcher m = LRC_PATTERN.matcher(raw);
            List<Long> times = new ArrayList<>();
            int end = 0;
            while (m.find() && m.start() == end) {
                long min = Long.parseLong(m.group(1));
                double sec = Double.parseDouble(m.group(2).replace(':', '.'));
                times.add(min * 60000L + Math.round(sec * 1000.0));
                end = m.end();
            }
            if (times.isEmpty()) continue;
            String text = raw.substring(end).trim();
            for (Long t : times) {
                lines.add(new LyricLine(t, text));
            }
        }
        lines.sort(Comparator.comparingLong(LyricLine::timeMs));
        return lines;
    }

    private static String httpGet(String urlStr) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URI(urlStr).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "SoundCraft-Minecraft-Mod/1.0");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            if (conn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    return sb.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
