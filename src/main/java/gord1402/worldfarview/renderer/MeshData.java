package gord1402.worldfarview.renderer;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

public class MeshData {
    private final VertexBuffer vertexBuffer;
    private boolean valid;

    private ResourceLocation texture;

    public MeshData(VertexBuffer buffer, ResourceLocation texture) {
        this.vertexBuffer = buffer;

        this.texture = texture;
        Minecraft.getInstance().getTextureManager().bindForSetup(texture);

        this.valid = true;
    }

    public void render(Matrix4f pose, Matrix4f projection) {
        if (!valid) return;

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        vertexBuffer.bind();
        vertexBuffer.drawWithShader(pose, projection, Objects.requireNonNull(RenderSystem.getShader()));
        VertexBuffer.unbind();
    }

    public void close() {
        if (valid) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer.close();
                Minecraft.getInstance().getTextureManager().release(texture);
            });
            valid = false;
        }
    }

    public boolean isValid() {
        return valid && vertexBuffer != null;
    }
}