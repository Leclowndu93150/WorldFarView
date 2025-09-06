package gord1402.worldfarview;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import gord1402.worldfarview.client.WorldFarPlaneClient;
import gord1402.worldfarview.config.Config;
import gord1402.worldfarview.network.ModNetworking;
import gord1402.worldfarview.network.PacketRequestServerConfiguration;
import gord1402.worldfarview.renderer.FarPlaneRenderer;
import gord1402.worldfarview.renderer.MeshData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientEvents {
    public static ShaderInstance FAR_TERRAIN_SHADER;

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Config.update();
    }

    @SubscribeEvent
    public void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        WorldFarPlaneClient.configDirty = true;
    }

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException
    {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), new ResourceLocation(WorldFarView.MODID, "far_terrain"), DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
                shader -> FAR_TERRAIN_SHADER = shader
        );
    }


    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onComputeFov(ViewportEvent.ComputeFov event){
            FarPlaneRenderer.FOV = event.getFOV();
        }
        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                FarPlaneRenderer.render(event.getPoseStack(), event.getCamera(), event.getPartialTick());
            }
        }

        @SubscribeEvent
        public static void onRenderFog(ViewportEvent.RenderFog event) {
            if (Config.disableVanillaFog) {
                event.setFarPlaneDistance(100000.0f);
                event.setNearPlaneDistance(90000.0f);
            }
        }

    }
}
