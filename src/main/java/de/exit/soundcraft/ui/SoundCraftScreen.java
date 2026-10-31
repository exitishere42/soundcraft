package de.exit.soundcraft.ui;

import de.exit.soundcraft.hud.DynamicIslandHud;
import de.exit.soundcraft.hud.SkyLyricsRenderer;
import de.exit.soundcraft.lyrics.LyricsService;
import de.exit.soundcraft.media.MediaManager;
import de.exit.soundcraft.media.TrackInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern In-Game Media Player Screen styled after OpenSoundboard,
 * with the dedicated color theme:
 * - Background/Panel: #1E1E24
 * - Accent/Primary: #3D7B7D
 * - Text/Secondary: #F4F4F6
 */
public class SoundCraftScreen extends Screen {

    private final List<Widget> widgets = new ArrayList<>();
    private Widget draggingWidget = null;

    private int frameX, frameY, frameW, frameH;
    private ModernSlider timelineSlider;
    private ButtonWidget playPauseBtn;

    public static TrackInfo currentTrack = TrackInfo.EMPTY;

    public SoundCraftScreen() {
        super(Component.literal("SoundCraft"));
    }

    @Override
    protected void init() {
        super.init();
        rebuildUi();
    }

    private void rebuildUi() {
        widgets.clear();

        // Responsive Panel sizing (like OpenSoundboard)
        frameW = Math.min(this.width - 20, 360);
        frameH = Math.min(this.height - 20, 240);
        frameX = (this.width - frameW) / 2;
        frameY = (this.height - frameH) / 2;

        int headerY = frameY;
        int closeBtnSize = 16;
        int closeBtnX = frameX + frameW - closeBtnSize - 4;
        int closeBtnY = headerY + (Theme.HEADER_H - closeBtnSize) / 2;

        // Close Button
        widgets.add(new ButtonWidget(closeBtnX, closeBtnY, closeBtnSize, closeBtnSize, "", Icons.CLOSE, this::onClose));

        // Player Controls Area
        int contentX = frameX + Theme.PAD;
        int contentY = frameY + Theme.HEADER_H + Theme.PAD;
        int contentW = frameW - Theme.PAD * 2;

        // Timeline Slider
        int sliderH = 14;
        int sliderY = contentY + 95;
        timelineSlider = new ModernSlider(contentX, sliderY, contentW, sliderH, currentTrack.progress(), val -> {
            // Seek action
        });
        widgets.add(timelineSlider);

        // Control Buttons (Previous, Play/Pause, Next)
        int btnW = 32;
        int btnH = 22;
        int controlsY = sliderY + sliderH + 8;
        int centerBtnX = frameX + frameW / 2;

        // Previous
        widgets.add(new ButtonWidget(centerBtnX - btnW - 20, controlsY, btnW, btnH, "", Icons.SKIP_BACK, MediaManager::previous));

        // Play / Pause (Accent Highlight #3D7B7D)
        Icons playIcon = currentTrack.isPlaying() ? Icons.PAUSE : Icons.PLAY;
        playPauseBtn = new ButtonWidget(centerBtnX - 18, controlsY - 1, 36, 24, "", playIcon, MediaManager::playPause).setPrimary(true);
        widgets.add(playPauseBtn);

        // Next
        widgets.add(new ButtonWidget(centerBtnX + 20, controlsY, btnW, btnH, "", Icons.SKIP_FORWARD, MediaManager::next));

        // Quick Feature Toggles (HUD Dynamic Island, Sky Lyrics, Ducking)
        int toggleY = controlsY + btnH + 12;
        int toggleW = (contentW - 8) / 3;

        widgets.add(new ButtonWidget(contentX, toggleY, toggleW, 18, "HUD Island", null, () -> {
            DynamicIslandHud.setEnabled(!DynamicIslandHud.isEnabled());
        }));

        widgets.add(new ButtonWidget(contentX + toggleW + 4, toggleY, toggleW, 18, "Sky Lyrics", null, () -> {
            SkyLyricsRenderer.setEnabled(!SkyLyricsRenderer.isEnabled());
        }));

        widgets.add(new ButtonWidget(contentX + (toggleW + 4) * 2, toggleY, toggleW, 18, "Ducking", null, () -> {
            // Sound Ducking toggle
        }));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        UiCanvas c = new UiCanvas(g, this.font, mouseX, mouseY);

        // 1. Scrim (Darkened World Background)
        c.fillRect(0, 0, this.width, this.height, Theme.SCRIM);

        // 2. Main Panel Background (#1E1E24)
        c.fillRoundRect(frameX, frameY, frameW, frameH, Theme.RADIUS, Theme.PANEL);
        c.roundBorder(frameX, frameY, frameW, frameH, Theme.RADIUS, Theme.BORDER);

        // 3. Header Bar (#18181D)
        c.fillRoundRect(frameX, frameY, frameW, Theme.HEADER_H, Theme.RADIUS, Theme.HEADER);
        c.border(frameX, frameY + Theme.HEADER_H - 1, frameW, 1, Theme.BORDER);

        // Header Title & App Name
        c.icon(Icons.NOTE, frameX + 6, frameY + (Theme.HEADER_H - Icons.NOTE.height) / 2, Theme.ACCENT);
        c.text("SoundCraft", frameX + 6 + Icons.NOTE.width + 5, frameY + (Theme.HEADER_H - c.lineHeight()) / 2 + 1, Theme.TEXT);

        if (!currentTrack.appName().isBlank()) {
            c.rightText(currentTrack.appName(), frameX + frameW - 26, frameY + (Theme.HEADER_H - c.lineHeight()) / 2 + 1, Theme.TEXT_MUTED);
        }

        // 4. Player Content Area
        int contentX = frameX + Theme.PAD;
        int contentY = frameY + Theme.HEADER_H + Theme.PAD;

        // Album Art Box (Square placeholder)
        int coverSize = 78;
        c.fillRoundRect(contentX, contentY, coverSize, coverSize, 4, Theme.SURFACE);
        c.roundBorder(contentX, contentY, coverSize, coverSize, 4, Theme.BORDER_STRONG);

        // Music Note icon placeholder
        c.icon(Icons.NOTE, contentX + (coverSize - Icons.NOTE.width) / 2, contentY + (coverSize - Icons.NOTE.height) / 2, Theme.TEXT_FAINT);

        // Track Details (Title, Artist, Album)
        int textLeft = contentX + coverSize + 10;
        int textMaxW = frameX + frameW - Theme.PAD - textLeft;

        String title = currentTrack.hasTrack() ? currentTrack.title() : "No Media Detected";
        String artist = currentTrack.hasTrack() ? currentTrack.artist() : "Play music in Spotify, Browser or VLC";
        String album = currentTrack.hasTrack() ? currentTrack.album() : "";

        // Track Title (Bold in #F4F4F6)
        c.text(c.trimText(title, textMaxW), textLeft, contentY + 6, Theme.TEXT);

        // Artist (in #3D7B7D Accent)
        c.text(c.trimText(artist, textMaxW), textLeft, contentY + 22, Theme.ACCENT_HOVER);

        // Album (in Text Muted)
        if (!album.isBlank()) {
            c.text(c.trimText(album, textMaxW), textLeft, contentY + 36, Theme.TEXT_MUTED);
        }

        // Live Lyrics Preview Snippet
        c.fillRoundRect(textLeft, contentY + 52, textMaxW, 26, 2, Theme.FIELD);
        c.roundBorder(textLeft, contentY + 52, textMaxW, 26, 2, Theme.BORDER);
        c.icon(Icons.LYRICS, textLeft + 4, contentY + 57, Theme.ACCENT);

        String activeLyric = LyricsService.getCurrentLine(currentTrack.positionMs());
        String lyricDisplay = activeLyric.isBlank() ? "Live lyrics scroll in sync with playback..." : activeLyric;
        c.text(c.trimText(lyricDisplay, textMaxW - 20), textLeft + 18, contentY + 60, activeLyric.isBlank() ? Theme.TEXT_MUTED : Theme.TEXT);

        // Timeline Timestamps
        int sliderY = contentY + 95;
        c.text(currentTrack.positionFormatted(), contentX, sliderY + 16, Theme.TEXT_MUTED);
        c.rightText(currentTrack.durationFormatted(), contentX + frameW - Theme.PAD * 2, sliderY + 16, Theme.TEXT_MUTED);

        // 5. Render Widgets
        for (Widget widget : widgets) {
            widget.render(c);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent e, boolean doubleClick) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget w = widgets.get(i);
            if (w.visible && w.active && w.contains(e.x(), e.y()) && w.mouseClicked(e.x(), e.y(), e.button())) {
                draggingWidget = w;
                return true;
            }
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent e, double dragX, double dragY) {
        if (draggingWidget != null) {
            draggingWidget.mouseDragged(e.x(), e.y(), e.button());
            return true;
        }
        return super.mouseDragged(e, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent e) {
        if (draggingWidget != null) {
            draggingWidget.mouseReleased(e.x(), e.y(), e.button());
            draggingWidget = null;
            return true;
        }
        return super.mouseReleased(e);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent e) {
        if (e.key() == 256 || e.key() == 74) { // ESC or J
            this.onClose();
            return true;
        }
        return super.keyPressed(e);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
