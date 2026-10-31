package de.exit.soundcraft;

import com.mojang.blaze3d.platform.InputConstants;
import de.exit.soundcraft.media.MediaManager;
import de.exit.soundcraft.ui.SoundCraftScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundCraftClient implements ClientModInitializer {
    public static final String MOD_ID = "soundcraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping openScreenKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[SoundCraft] Initializing universal media player & HUD...");

        // 1. Initialize Cross-Platform Media Manager
        MediaManager.init();

        // 2. Register Keybinding 'J' (Minecraft 26.3 / Fabric)
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "general"));
        openScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.soundcraft.open",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_J,
                category
        ));

        // 3. Listen for Key Press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new SoundCraftScreen());
                }
            }
        });

        LOGGER.info("[SoundCraft] Initialized successfully with custom OpenSoundboard theme.");
    }
}
