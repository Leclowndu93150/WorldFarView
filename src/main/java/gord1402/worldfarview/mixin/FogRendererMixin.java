package gord1402.worldfarview.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import gord1402.worldfarview.config.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void disableVanillaFog(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (Config.DISABLE_VANILLA_FOG.get()) {
            RenderSystem.setShaderFogStart(100000.0f);
            RenderSystem.setShaderFogEnd(200000.0f);
            ci.cancel();
        }
    }
}