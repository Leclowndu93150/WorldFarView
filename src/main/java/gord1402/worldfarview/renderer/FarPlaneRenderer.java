package gord1402.worldfarview.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import gord1402.worldfarview.FarPlaneLOD;
import gord1402.worldfarview.ModClientEvents;
import gord1402.worldfarview.WorldFarView;
import gord1402.worldfarview.client.WorldFarPlaneClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class FarPlaneRenderer {
    public static double FOV = 70.;

    public static void render(PoseStack poseStack, Camera camera, float partialTick) {
        if (WorldFarPlaneClient.disabled) return;
        Vec3 camPos = camera.getPosition();

        Matrix4f originalProjection = RenderSystem.getProjectionMatrix();
        Matrix4f farProjection = new Matrix4f();
        float fov = (float) FOV; // degrees
        float aspectRatio = (float) Minecraft.getInstance().getWindow().getWidth() / Minecraft.getInstance().getWindow().getHeight();
        float near = 0.05f;
        float far = 40000000.0f; // Increase far plane here

        farProjection.setPerspective(fov * ((float) Math.PI / 180F), aspectRatio, near, far);
        RenderSystem.setProjectionMatrix(farProjection, VertexSorting.DISTANCE_TO_ORIGIN);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        PoseStack bobStack = new PoseStack();
        bobHurt(bobStack, camera.getEntity(), partialTick);
        if (Minecraft.getInstance().options.bobView().get()) {
            bobView(bobStack, camera.getEntity(), partialTick);
        }
        bobStack.mulPose(Axis.ZP.rotationDegrees(0));
        bobStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        bobStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        bobStack.translate(-camPos.x, -camPos.y, -camPos.z);

        ShaderInstance shader = ModClientEvents.FAR_TERRAIN_SHADER;

        if (shader != null) {
            Uniform sunDirUniform = shader.getUniform("SunDirection");
            if (sunDirUniform != null) {
                float celestialAngle = camera.getEntity().level().getTimeOfDay(partialTick);
                float sunYaw = 0;
                float sunPitch = (float)(Math.cos(celestialAngle * 2 * Math.PI) * Math.PI / 2.0);



                Vec3 sunDirection = new Vec3(
                        Math.cos(sunYaw) * Math.cos(sunPitch),
                        Math.sin(sunPitch),
                        Math.sin(sunYaw) * Math.cos(sunPitch)
                );
                sunDirUniform.set((float)sunDirection.x, (float)sunDirection.y, (float)sunDirection.z);
            }
        }

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        Matrix4f matrix = bobStack.last().pose();

        for (FarPlaneLOD lod : WorldFarPlaneClient.getLods()) {
            if (lod == null) continue;
            MeshData mesh = lod.getOrBuild();
            if (mesh != null && mesh.isValid()) {
                mesh.render(matrix, farProjection);
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setProjectionMatrix(originalProjection, VertexSorting.DISTANCE_TO_ORIGIN);
    }

    public static void bobHurt(PoseStack poseStack, Entity cameraEntity,  float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (cameraEntity instanceof LivingEntity livingEntity) {
            float f = (float)livingEntity.hurtTime - partialTick;
            if (livingEntity.isDeadOrDying()) {
                float f1 = Math.min((float)livingEntity.deathTime + partialTick, 20.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (f1 + 200.0F)));
            }

            if (f < 0.0F) {
                return;
            }

            f /= (float)livingEntity.hurtDuration;
            f = Mth.sin(f * f * f * f * (float)Math.PI);
            float f3 = livingEntity.getHurtDir();
            poseStack.mulPose(Axis.YP.rotationDegrees(-f3));
            float f2 = (float)((double)(-f) * 14.0D * minecraft.options.damageTiltStrength().get());
            poseStack.mulPose(Axis.ZP.rotationDegrees(f2));
            poseStack.mulPose(Axis.YP.rotationDegrees(f3));
        }

    }

    public static void bobView(PoseStack poseStack, Entity cameraEntity, float partialTick) {
        if (cameraEntity instanceof Player player) {
            float f = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + f * partialTick);
            float f2 = Mth.lerp(partialTick, player.oBob, player.bob);
            poseStack.translate(Mth.sin(f1 * (float)Math.PI) * f2 * 0.5F, -Math.abs(Mth.cos(f1 * (float)Math.PI) * f2), 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float)Math.PI) * f2 * 3.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
        }
    }
}