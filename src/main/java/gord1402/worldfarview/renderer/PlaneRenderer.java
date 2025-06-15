package gord1402.worldfarview.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class PlaneRenderer {
    public static void renderPlane(PoseStack poseStack, Camera camera) {
        Vec3 camPos = camera.getPosition();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        float y = 64;
        int halfSize = 5;

        float r = 0.0f, g = 0.5f, b = 1.0f, a = 1.f;
        buffer.vertex(matrix, -halfSize, y, -halfSize).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, -halfSize, y, halfSize).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, halfSize, y, halfSize).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, halfSize, y, -halfSize).color(r, g, b, a).endVertex();

        tesselator.end();

        poseStack.popPose();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

}
