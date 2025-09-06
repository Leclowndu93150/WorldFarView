package gord1402.worldfarview.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks actually rendered chunks to provide dynamic LOD boundaries
 * that adapt smoothly based on real rendering rather than just render distance
 */
@OnlyIn(Dist.CLIENT)
public class ChunkRenderTracker {
    private static final AtomicReference<Float> currentBoundary = new AtomicReference<>(512.0f);
    private static final AtomicReference<Float> targetBoundary = new AtomicReference<>(512.0f);
    
    // Smooth adaptation parameters (will be updated from config)
    private static float ADAPTATION_SPEED = 0.05f; // How fast to adapt (0.01 = slow, 0.1 = fast)
    private static float MIN_BOUNDARY = 256.0f;
    private static float MAX_BOUNDARY = 2048.0f;
    private static final float BOUNDARY_MARGIN = 64.0f; // Extra margin beyond furthest chunk
    
    // Reflection fields for accessing Minecraft internals
    private static Field viewAreaField;
    private static Field chunksField;
    
    static {
        try {
            // Try to get ViewArea field from LevelRenderer
            viewAreaField = LevelRenderer.class.getDeclaredField("viewArea");
            viewAreaField.setAccessible(true);
            
            // Try to get chunks array from ViewArea
            chunksField = ViewArea.class.getDeclaredField("chunks");
            chunksField.setAccessible(true);
        } catch (Exception e) {
            // If reflection fails, we'll fall back to render distance
            System.err.println("Failed to setup chunk render tracking reflection: " + e.getMessage());
        }
    }
    
    /**
     * Updates the target boundary based on actually rendered chunks
     */
    public static void updateBoundary() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        
        // Update parameters from config
        updateFromConfig();
        
        float newBoundary = calculateActualRenderBoundary(mc);
        if (newBoundary > 0) {
            targetBoundary.set(Mth.clamp(newBoundary, MIN_BOUNDARY, MAX_BOUNDARY));
        }
        
        // Smoothly interpolate current boundary towards target
        float current = currentBoundary.get();
        float target = targetBoundary.get();
        float newCurrent = Mth.lerp(ADAPTATION_SPEED, current, target);
        currentBoundary.set(newCurrent);
    }
    
    /**
     * Calculates the actual render boundary based on rendered chunks
     */
    private static float calculateActualRenderBoundary(Minecraft mc) {
        try {
            if (viewAreaField == null || chunksField == null) {
                // Fallback to render distance if reflection failed
                return mc.options.renderDistance().get() * 16.0f + BOUNDARY_MARGIN;
            }
            
            LevelRenderer levelRenderer = mc.levelRenderer;
            ViewArea viewArea = (ViewArea) viewAreaField.get(levelRenderer);
            
            if (viewArea == null) return -1;
            
            ChunkRenderDispatcher.RenderChunk[] chunks = (ChunkRenderDispatcher.RenderChunk[]) chunksField.get(viewArea);
            if (chunks == null) return -1;
            
            Entity camera = mc.getCameraEntity();
            if (camera == null) return -1;
            
            Vec3 cameraPos = camera.position();
            float maxDistanceSquared = 0;
            int renderedChunks = 0;
            
            // Find the furthest actually rendered chunk
            for (ChunkRenderDispatcher.RenderChunk chunk : chunks) {
                if (chunk != null) {
                    ChunkRenderDispatcher.CompiledChunk compiledChunk = chunk.getCompiledChunk();
                    if (compiledChunk != null && !compiledChunk.hasNoRenderableLayers()) {
                        // Get chunk center position
                        double chunkCenterX = (chunk.getOrigin().getX() + 8);
                        double chunkCenterZ = (chunk.getOrigin().getZ() + 8);
                        
                        float distanceSquared = (float) cameraPos.distanceToSqr(chunkCenterX, cameraPos.y, chunkCenterZ);
                        if (distanceSquared > maxDistanceSquared) {
                            maxDistanceSquared = distanceSquared;
                        }
                        renderedChunks++;
                    }
                }
            }
            
            if (renderedChunks == 0) return -1;
            
            // Convert to actual distance and add margin
            return (float) Math.sqrt(maxDistanceSquared) + BOUNDARY_MARGIN;
            
        } catch (Exception e) {
            // Fallback to render distance if anything goes wrong
            return mc.options.renderDistance().get() * 16.0f + BOUNDARY_MARGIN;
        }
    }
    
    /**
     * Gets the current smoothly adapting boundary distance
     */
    public static float getCurrentBoundary() {
        return currentBoundary.get();
    }
    
    /**
     * Gets the target boundary (what we're adapting towards)
     */
    public static float getTargetBoundary() {
        return targetBoundary.get();
    }
    
    /**
     * Forces an immediate boundary update (useful for config changes)
     */
    public static void forceUpdate() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        
        float newBoundary = calculateActualRenderBoundary(mc);
        if (newBoundary > 0) {
            float clampedBoundary = Mth.clamp(newBoundary, MIN_BOUNDARY, MAX_BOUNDARY);
            currentBoundary.set(clampedBoundary);
            targetBoundary.set(clampedBoundary);
        }
    }
    
    /**
     * Gets the distance where LOD meshes should start (just beyond rendered chunks)
     */
    public static float getMeshStartDistance() {
        return getCurrentBoundary();
    }
    
    /**
     * Gets the distance where the crossfade should start (slightly before mesh start)
     */
    public static float getCrossfadeStartDistance(float crossfadeDistance) {
        return Math.max(0, getCurrentBoundary() - crossfadeDistance);
    }
    
    /**
     * Updates internal parameters from config values
     */
    private static void updateFromConfig() {
        try {
            // Use reflection to access Config values to avoid circular dependency
            Class<?> configClass = Class.forName("gord1402.worldfarview.config.Config");
            ADAPTATION_SPEED = (float) configClass.getField("adaptationSpeed").getDouble(null);
            MIN_BOUNDARY = (float) configClass.getField("minBoundary").getInt(null);
            MAX_BOUNDARY = (float) configClass.getField("maxBoundary").getInt(null);
        } catch (Exception e) {
            // Keep default values if config access fails
        }
    }
}