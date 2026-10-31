# Mod-Spezifikation: SoundCraft (`soundcraft`)

> **An den ausführenden Entwickler-Agenten:**
> Dieses Dokument ist deine verbindliche Spezifikation für die Fabric-Minecraft-Mod **SoundCraft**.
> Halte dich strikt an die Versionsvorgaben, die Cross-Platform-Anforderungen (Windows & Linux) und die Design-Richtlinien.
> Verifikation: `./gradlew build` muss fehlerfrei durchlaufen.

---

## 1. Meta-Informationen & Rahmenbedingungen

* **Mod-Name:** SoundCraft
* **Mod-ID:** `soundcraft`
* **Base-Package:** `de.exit.soundcraft`
* **Minecraft-Version:** `26.3` (Java 25, unobfuscated Mojang mappings)
* **Mod-Loader:** Fabric Loader (`>= 0.17.x`) + Fabric API
* **Umgebung (Environment):** `client` (**Client-Only Mod** – keine Server-Installation erforderlich, voll kompatibel auf allen Singleplayer- & Multiplayer-Servern)
* **Design & Farbpalette:**
  * **Hintergrund (Surface):** `#1E1E24`
  * **Primärfarbe (Akzent):** `#3D7B7D` (Deep Slate Teal)
  * **Sekundär / Text (Foreground):** `#F4F4F6` (Clean Off-White)
  * **Border / Divider:** `#2D2D36`

---

## 2. Kernarchitektur & Komponenten

```
de.exit.soundcraft/
├── SoundCraftClient.java                  # ClientModInitializer (Keybindings, Event-Registrierung)
├── config/
│   ├── SoundCraftConfig.java             # Persistierte JSON-Konfiguration (GSON)
│   └── ConfigManager.java
├── media/
│   ├── MediaManager.java                 # Abstraktionsschicht & Polling/Event-Hub
│   ├── TrackInfo.java                    # Immutable Record (Title, Artist, Album, Duration, Position, Playing, Cover)
│   ├── MediaController.java              # Interface für Play, Pause, Next, Previous
│   ├── platform/
│   │   ├── WindowsMediaProvider.java     # GSMTC via PowerShell / native Windows Runtime
│   │   └── LinuxMediaProvider.java       # MPRIS via D-Bus (org.mpris.MediaPlayer2)
│   └── cover/
│       └── CoverTextureManager.java      # Async Bitmap-Laden in NativeImage / DynamicTexture
├── lyrics/
│   ├── LyricsService.java                # REST-Client für https://lrclib.net/api/get
│   ├── SyncedLyrics.java                 # Datenstruktur für zeitgestempelte Textzeilen
│   └── LyricsSyncEngine.java             # Berechnet aktuelle Textzeile anhand von TrackInfo.position
├── hud/
│   ├── DynamicIslandHud.java             # Oben zentriertes Widget (Spring-Animation, Cover, Titel, Fortschritt)
│   ├── SkyLyricsRenderer.java            # 3D-Welt-Rendering für schwebende Lyrics am Himmel
│   └── HudPosition.java                  # TOP_CENTER, TOP_LEFT, BOTTOM_RIGHT etc.
├── gui/
│   ├── SoundCraftScreen.java             # Ingame-Player- & Einstellungsmenü (Taste J)
│   └── widgets/
│       ├── PlayerControlsWidget.java     # Play/Pause, Next, Prev, Timeline-Slider
│       └── ModernSliderWidget.java       # Haptischer Slider im Farbschema
└── mixin/
    └── SoundSystemMixin.java             # Ducking: Pausiert Minecraft-Musik wenn externe Musik läuft
```

---

## 3. Detail-Spezifikation der Kernmodule

### A. Universelle Mediensteuerung (Windows & Linux)
* **Ziel:** Musik aus **allen** gängigen Desktop-Playern und Browsern erkennen (Spotify, YouTube in Chrome/Firefox/Edge, Apple Music, VLC), **ohne** Spotify-Developer-Account und ohne API-Token.
* **Windows Provider (`WindowsMediaProvider`):**
  * Verwendet `Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager` (GSMTC) über einen leichtgewichtigen Worker-Prozess.
  * Liest `Title`, `Artist`, `AlbumTitle`, `Position`, `EndTime` (Dauer) und das Cover-Thumbnail aus.
  * Unterstützt Steuerbefehle: Play, Pause, Next, Previous.
* **Linux Provider (`LinuxMediaProvider`):**
  * Kommuniziert über D-Bus mit `org.mpris.MediaPlayer2.*`.
  * Fragt Metadaten (`Metadata`) und Playback-Status (`PlaybackStatus`) ab.
  * Sendet Steuersignale (`PlayPause`, `Next`, `Previous`).

### B. Sound-Ducking (`SoundSystemMixin`)
* Wenn `TrackInfo.isPlaying()` aktiv ist und der Nutzer Musik hört, wird die Standard-Minecraft-Hintergrundmusik sanft stummgeschaltet oder pausiert.
* Sobald die externe Musik pausiert oder stoppt, wird die Minecraft-Musik wieder wie gewohnt freigegeben.

### C. Live-Lyrics via LRCLIB (`LyricsService`)
* Ruft Lyrics asynchron über `https://lrclib.net/api/get?artist_name={artist}&track_name={title}&album_name={album}&duration={duration}` ab.
* Parsed die LRC-Timestamps `[mm:ss.xx]`.
* Stellt die jeweils aktive Textzeile in Echtzeit für das HUD und das 3D-Himmelsrendering bereit.

### D. Dynamic Island HUD (`DynamicIslandHud`)
* Dezentes, abgerundetes Floating-Widget am oberen Bildschirmrand.
* Reagiert auf Songwechsel mit einer weichen Spring-/Expand-Animation.
* Zeigt:
  * 24x24 px Cover-Artwork (mit abgerundeten Ecken)
  * Songtitel in `#F4F4F6` (Bold)
  * Künstler in `#8E8EA0`
  * 2px Fortschrittsbalken in Primärfarbe `#3D7B7D`
  * Aktuelle Textzeile (optional einblendbar)

### E. Ingame-Player- & Einstellungs-Screen (`SoundCraftScreen`)
* Öffnet sich standardmäßig über Taste `J` (im Minecraft-Tastaturmenü konfigurierbar).
* Dunkler Hintergrund `#1E1E24` mit 4px abgerundeten Karten-Containern.
* Vollwertige Wiedergabesteuerung: Großer Play/Pause-Button, Zurück, Weiter, seekbare Timeline.
* Einstellungen:
  * HUD an/aus, Position & Skalierung
  * Sky-Lyrics an/aus & Transparenz
  * Sound-Ducking Intensität
  * Lyrics Zeitoffset (+/- ms)

---

## 4. Definition of Done (DoD)

1. **Gradle Build:** `./gradlew build` kompiliert ohne Fehler oder Warnungen.
2. **Plattform-Sicherheit:**
   * Unter Windows werden Windows-Media-APIs genutzt.
   * Unter Linux wird sauber auf D-Bus/MPRIS zugegriffen.
   * Keine unbehandelten Exceptions, falls kein Mediaplayer aktiv ist.
3. **Stabilität:**
   * Async I/O: Alle HTTP- (LRCLIB) und Cover-Ladevorgänge laufen in separaten Daemon-Threads; der Minecraft-Render-Thread wird **niemals** blockiert.
   * Keine memory leaks bei dynamisch registrierten Cover-Texturen (alte Texturen werden mit `destroy()` freigegeben).
4. **Multiplayer-Sicherheit:** Da die Mod rein clientseitig ist (`environment: client`), gibt es keine Server-Crashes oder Netzwerk-Inkompatibilitäten.
