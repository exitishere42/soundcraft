package de.exit.soundcraft.mixin;

import de.exit.soundcraft.hud.DynamicIslandHud;
import de.exit.soundcraft.hud.SkyLyricsRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract boolean isHidden();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void soundcraft$renderHud(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!this.isHidden() && this.minecraft.gui.screen() == null) {
            int sw = this.minecraft.getWindow().getGuiScaledWidth();
            int sh = this.minecraft.getWindow().getGuiScaledHeight();

            // 1. Dynamic Island at top
            DynamicIslandHud.render(extractor, this.minecraft.font, sw, sh);

            // 2. Floating Sky Lyrics
            SkyLyricsRenderer.render(extractor, this.minecraft.font, sw, sh);
        }
    }
}
