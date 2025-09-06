package gord1402.worldfarview.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import gord1402.worldfarview.FarPlaneLOD;
import gord1402.worldfarview.ModClientEvents;
import gord1402.worldfarview.WorldFarView;
import gord1402.worldfarview.client.ChunkRenderTracker;
import gord1402.worldfarview.client.WorldFarPlaneClient;
import gord1402.worldfarview.config.Config;
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
        if (WorldFarPlaneClient.disabled) {
            return;
        }

        ShaderInstance shader = ModClientEvents.FAR_TERRAIN_SHADER;
        if (shader == null) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        Matrix4f originalProjection = RenderSystem.getProjectionMatrix();

        Matrix4f farProjection = new Matrix4f();
        float fovRad = (float) FOV;
        float aspectRatio = (float) Minecraft.getInstance().getWindow().getWidth() /
                (float) Minecraft.getInstance().getWindow().getHeight();
        float near = 0.05f;
        float far = 40000000.0f;
        farProjection.setPerspective(fovRad * 0.017453292f, aspectRatio, near, far);

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

        bobStack.mulPose(Axis.ZP.rotationDegrees(0.0f));
        bobStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        bobStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));
        bobStack.translate(-camPos.x, -camPos.y, -camPos.z);

        setupShaderUniforms(shader, camera, partialTick);

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        Matrix4f matrix = bobStack.last().pose();

        FarPlaneLOD[] lods = WorldFarPlaneClient.getLods();
        if (lods != null) {
            for (FarPlaneLOD lod : lods) {
                if (lod != null && shouldRenderLOD(lod, camPos)) {
                    MeshData mesh = lod.getOrBuild();
                    if (mesh != null && mesh.isValid()) {
                        mesh.render(matrix, farProjection);
                    }
                }
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.setProjectionMatrix(originalProjection, VertexSorting.DISTANCE_TO_ORIGIN);
    }

    private static void setupShaderUniforms(ShaderInstance shader, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        Uniform sunDirUniform = shader.getUniform("SunDirection");
        if (sunDirUniform != null) {
            float celestialAngle = camera.getEntity().level().getTimeOfDay(partialTick);
            float sunYaw = 0.0f;
            float sunPitch = (float)(Math.cos(celestialAngle * 2.0f * Math.PI) * Math.PI / 2.0);
            Vec3 sunDirection = new Vec3(
                    Math.cos(sunYaw) * Math.cos(sunPitch),
                    Math.sin(sunPitch),
                    Math.sin(sunYaw) * Math.cos(sunPitch)
            );
            sunDirUniform.set((float)sunDirection.x, (float)sunDirection.y, (float)sunDirection.z);
        }

        float renderDistance = mc.options.renderDistance().get() * 16.0f;

        Uniform fogStartUniform = shader.getUniform("CustomFogStart");
        if (fogStartUniform != null) {
            fogStartUniform.set(renderDistance * (float) Config.customFogStart);
        }

        Uniform fogEndUniform = shader.getUniform("CustomFogEnd");
        if (fogEndUniform != null) {
            fogEndUniform.set(renderDistance * (float) Config.customFogEnd);
        }

        Uniform enableFogUniform = shader.getUniform("EnableCustomFog");
        if (enableFogUniform != null) {
            enableFogUniform.set(Config.disableVanillaFog ? 1.0f : 0.0f);
        }

        Uniform enableCrossfadeUniform = shader.getUniform("EnableCrossfade");
        if (enableCrossfadeUniform != null) {
            enableCrossfadeUniform.set(Config.enableCrossfade ? 1.0f : 0.0f);
        }

        float meshStart = getMeshStartDistance();
        float crossfadeDistance = (float) Config.crossfadeDistance;

        Uniform crossfadeStartUniform = shader.getUniform("CrossfadeStart");
        if (crossfadeStartUniform != null) {
            crossfadeStartUniform.set(meshStart - crossfadeDistance);
        }

        Uniform crossfadeEndUniform = shader.getUniform("CrossfadeEnd");
        if (crossfadeEndUniform != null) {
            crossfadeEndUniform.set(meshStart);
        }
    }

    private static boolean shouldRenderLOD(FarPlaneLOD lod, Vec3 camPos) {
        if (lod.getLodIndex() == 0 && (Config.autoFitRenderDistance || Config.meshStartDistance > 0)) {
            double distanceToCenter = Math.sqrt(
                    Math.pow(camPos.x - lod.centerX, 2) +
                            Math.pow(camPos.z - lod.centerZ, 2)
            );

            float meshStartDistance = getMeshStartDistance();
            return distanceToCenter >= meshStartDistance - lod.getSize() / 2;
        }
        return true;
    }

    private static float getMeshStartDistance() {
        if (Config.smoothAdaptation) {
            return ChunkRenderTracker.getMeshStartDistance();
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (Config.autoFitRenderDistance || Config.meshStartDistance == 0) {
                return mc.options.renderDistance().get() * 16.0f;
            } else {
                return (float) Config.meshStartDistance;
            }
        }
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