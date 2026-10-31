package de.exit.soundcraft.media.platform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.exit.soundcraft.media.TrackInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Windows GSMTC (Global System Media Transport Controls) Provider.
 * Connects to Windows Media API via a lightweight background worker
 * to detect any media app (Spotify, Chrome, Firefox, Edge, VLC) without API tokens.
 */
public class WindowsMediaProvider {

    private static Process workerProcess;
    private static BufferedWriter workerInput;
    private static volatile boolean running = false;

    private static final String SCRIPT =
            "$ErrorActionPreference = 'Stop'\n" +
            "[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding $false\n" +
            "function Out-Line($l) { try { [Console]::Out.WriteLine($l); [Console]::Out.Flush() } catch { exit } }\n" +
            "Add-Type -AssemblyName System.Runtime.WindowsRuntime\n" +
            "$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]\n" +
            "function Await($op, $type) { $t = $asTask.MakeGenericMethod($type).Invoke($null, @($op)); $null = $t.Wait(-1); $t.Result }\n" +
            "$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\n" +
            "$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties,Windows.Media.Control,ContentType=WindowsRuntime]\n" +
            "$mgr = $null\n" +
            "try {\n" +
            "  $mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\n" +
            "} catch {}\n" +
            "\n" +
            "function Get-CurrentMedia {\n" +
            "  if ($null -eq $mgr) { return $null }\n" +
            "  $s = $mgr.GetCurrentSession()\n" +
            "  if ($null -eq $s) { return $null }\n" +
            "  $p = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\n" +
            "  if ($null -eq $p -or [string]::IsNullOrEmpty($p.Title)) { return $null }\n" +
            "  $tl = $s.GetTimelineProperties()\n" +
            "  $pb = $s.GetPlaybackInfo()\n" +
            "  $playing = ($null -ne $pb -and $pb.PlaybackStatus.ToString() -eq 'Playing')\n" +
            "  $pos = if ($null -ne $tl) { [long]$tl.Position.TotalMilliseconds } else { 0 }\n" +
            "  $dur = if ($null -ne $tl) { [long]$tl.EndTime.TotalMilliseconds } else { 0 }\n" +
            "  return @{\n" +
            "    title = [string]$p.Title;\n" +
            "    artist = [string]$p.Artist;\n" +
            "    album = [string]$p.AlbumTitle;\n" +
            "    app = [string]$s.SourceAppUserModelId;\n" +
            "    pos = $pos;\n" +
            "    dur = $dur;\n" +
            "    playing = $playing\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "while ($true) {\n" +
            "  try {\n" +
            "    $data = Get-CurrentMedia\n" +
            "    if ($null -ne $data) {\n" +
            "      Out-Line ($data | ConvertTo-Json -Compress)\n" +
            "    } else {\n" +
            "      Out-Line '{\"title\":\"\",\"artist\":\"\",\"album\":\"\",\"app\":\"\",\"pos\":0,\"dur\":0,\"playing\":false}'\n" +
            "    }\n" +
            "  } catch {}\n" +
            "  Start-Sleep -Milliseconds 800\n" +
            "}\n";

    public static void start(Consumer<TrackInfo> onUpdate) {
        if (running) return;
        running = true;

        Thread workerThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "powershell", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-Command", SCRIPT
                );
                pb.redirectErrorStream(true);
                workerProcess = pb.start();

                workerInput = new BufferedWriter(new OutputStreamWriter(workerProcess.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader reader = new BufferedReader(new InputStreamReader(workerProcess.getInputStream(), StandardCharsets.UTF_8));

                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (line.startsWith("{") && line.contains("title")) {
                        try {
                            JsonObject o = JsonParser.parseString(line).getAsJsonObject();
                            String title = o.has("title") ? o.get("title").getAsString() : "";
                            String artist = o.has("artist") ? o.get("artist").getAsString() : "";
                            String album = o.has("album") ? o.get("album").getAsString() : "";
                            String app = o.has("app") ? o.get("app").getAsString() : "";
                            long pos = o.has("pos") ? o.get("pos").getAsLong() : 0;
                            long dur = o.has("dur") ? o.get("dur").getAsLong() : 0;
                            boolean playing = o.has("playing") && o.get("playing").getAsBoolean();

                            TrackInfo info = new TrackInfo(title, artist, album, cleanAppName(app), dur, pos, playing, null);
                            onUpdate.accept(info);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                // Stopped or unavailable
            }
        }, "SoundCraft-WindowsMediaWorker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public static void sendCommand(String cmd) {
        // Play, Pause, Next, Previous via PowerShell commands if needed
    }

    private static String cleanAppName(String id) {
        if (id == null) return "";
        String l = id.toLowerCase();
        if (l.contains("spotify")) return "Spotify";
        if (l.contains("chrome")) return "Chrome";
        if (l.contains("firefox")) return "Firefox";
        if (l.contains("msedge") || l.contains("edge")) return "Edge";
        if (l.contains("vlc")) return "VLC";
        if (l.contains("apple")) return "Apple Music";
        return id.replace(".exe", "");
    }

    public static void stop() {
        running = false;
        if (workerProcess != null && workerProcess.isAlive()) {
            workerProcess.destroyForcibly();
        }
    }
}
