package gord1402.worldfarview.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

import java.util.Objects;

public class MeshData {
    private final VertexBuffer vertexBuffer;
    private boolean valid;
    public MeshData(VertexBuffer buffer) {
        this.vertexBuffer = buffer;
        this.valid = true;
    }

    public void render(Matrix4f pose, Matrix4f projection) {
        if (!valid) return;

        vertexBuffer.bind();
        vertexBuffer.drawWithShader(pose, projection, Objects.requireNonNull(RenderSystem.getShader()));
        VertexBuffer.unbind();
    }

    public void close() {
        if (valid) {
            RenderSystem.recordRenderCall(vertexBuffer::close);
            valid = false;
        }
    }

    public boolean isValid() {
        return valid && vertexBuffer != null;
    }
}