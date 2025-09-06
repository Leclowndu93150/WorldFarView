package gord1402.worldfarview.mixin;

import gord1402.worldfarview.config.Config;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyArg(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V"),
            index = 2
    )
    private float extendFogDistance(float viewDistance) {
        if (Config.DISABLE_VANILLA_FOG.get()) {
            return 100000.0f;
        }
        return viewDistance;
    }
}