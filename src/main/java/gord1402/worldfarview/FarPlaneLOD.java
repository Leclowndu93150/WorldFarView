package gord1402.worldfarview;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.*;
import gord1402.worldfarview.client.ClientFarChunkGenerator;
import gord1402.worldfarview.network.ModNetworking;
import gord1402.worldfarview.network.PacketLODUpdate;
import gord1402.worldfarview.registry.FarChunkGenerators;
import gord1402.worldfarview.renderer.MeshData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class FarPlaneLOD {
    private final int resolution;
    private final int size;
    private int[] heightMap;
    private int[] colorMap;
    private final int lodIndex;

    public final int arraySize;

    public int centerX = 0;
    public int centerZ = 0;

    private MeshData cachedMesh;
    private boolean dirty = true;


    public void markDirty() {
        this.dirty = true;
    }

    public FarPlaneLOD(int lodIndex, int resolution, int size) {
        this.lodIndex = lodIndex;
        this.resolution = resolution;
        this.size = size;

        int arraySize = 1;

        for (int x = -size; x < size; x += size / resolution) {
            for (int z = -size; z < size; z += size / resolution) {
                if (Math.abs(x) < (size / 2 - size / resolution) && Math.abs(z) < (size / 2 - size / resolution)) {
                    continue;
                }
                arraySize++;
            }
        }

        this.arraySize = arraySize;

        WorldFarView.LOGGER.info("LOD {} requires array {}", lodIndex, arraySize);

        this.heightMap = new int[arraySize];
        this.colorMap = new int[arraySize];
    }

    public static void generateOnServer(ServerPlayer player, ServerLevel level, int lodIndex, int centerX, int centerZ, int resolution, int size) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        FarChunkGenerator farChunkGenerator = FarChunkGenerators.getServer(generator);

        if (farChunkGenerator == null) {
            WorldFarView.LOGGER.error("Not found far chunk generator for {}", generator.getClass());
            return;
        }

        farChunkGenerator.init(generator, level);

        int arraySize = 1;
        for (int x = -size; x < size; x += size / resolution) {
            for (int z = -size; z < size; z += size / resolution) {
                if (Math.abs(x) < (size / 2 - size / resolution) && Math.abs(z) < (size / 2 - size / resolution)) {
                    continue;
                }
                arraySize++;
            }
        }

        int index = 0;

        int[] flatHeightMap = new int[arraySize];
        int[] colorMap = new int[arraySize];

        for (int x = -size; x < size; x += size / resolution) {
            for (int z = -size; z < size; z += size / resolution) {
                if (Math.abs(x) < (size / 2 - size / resolution) && Math.abs(z) < (size / 2 - size / resolution)) {
                    continue;
                }

                int worldX = centerX + x;
                int worldZ = centerZ + z;

                int height = farChunkGenerator.getHeightAt(worldX, worldZ);

                flatHeightMap[index] = height;
                colorMap[index] = farChunkGenerator.getHexColorAt(worldX, height, worldZ);

                index++;
            }
        }

        PacketLODUpdate pkt = new PacketLODUpdate(lodIndex, centerX, centerZ, resolution, size, flatHeightMap, colorMap);
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), pkt);


    }

    public static PacketLODUpdate generateOnClient(ClientFarChunkGenerator farChunkGenerator, long seed, int lodIndex, int centerX, int centerZ, int resolution, int size) {
        farChunkGenerator.init(seed);

        int arraySize = 1;
        for (int x = -size; x < size; x += size / resolution) {
            for (int z = -size; z < size; z += size / resolution) {
                if (Math.abs(x) < (size / 2 - size / resolution) && Math.abs(z) < (size / 2 - size / resolution)) {
                    continue;
                }
                arraySize++;
            }
        }

        int index = 0;

        int[] flatHeightMap = new int[arraySize];
        int[] colorMap = new int[arraySize];

        for (int x = -size; x < size; x += size / resolution) {
            for (int z = -size; z < size; z += size / resolution) {
                if (Math.abs(x) < (size / 2 - size / resolution) && Math.abs(z) < (size / 2 - size / resolution)) {
                    continue;
                }

                int worldX = centerX + x;
                int worldZ = centerZ + z;

                int height = farChunkGenerator.getHeightAt(worldX, worldZ);

                flatHeightMap[index] = height;
                colorMap[index] = farChunkGenerator.getHexColorAt(worldX, height, worldZ);

                index++;
            }
        }

        PacketLODUpdate pkt = new PacketLODUpdate(lodIndex, centerX, centerZ, resolution, size, flatHeightMap, colorMap);

        return pkt;
    }


    public MeshData getOrBuild() {
        if (dirty || cachedMesh == null || !cachedMesh.isValid()) {
            if (!buildMesh()) return null;
        }
        return cachedMesh;
    }


    public boolean buildMesh() {
        if (heightMap == null || colorMap == null) return false;

        MeshData newMesh = createNewMesh();

        if (newMesh != null) {
            if (cachedMesh != null) {
                cachedMesh.close();
            }
            cachedMesh = newMesh;
            dirty = false;
            return true;
        }

        return false;
    }

    private MeshData createNewMesh() {
        WorldFarView.LOGGER.info("Building mesh data for {}", lodIndex);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        try {
            int step = size / resolution;

            // Bake texture
            DynamicTexture dynamicTexture = new DynamicTexture(
                    (int) Math.ceil((2. * size) / step),
                    (int) Math.ceil((2. * size) / step),
                    false);
            NativeImage image = dynamicTexture.getPixels();

            int index = 0;
            int tx = 0, tz = 0;

            for (int wx = -size; wx < size; wx += step) {
                for (int wz = -size; wz < size; wz += step) {
                    tz ++;
                    if (Math.abs(wx) < (size / 2 - step) && Math.abs(wz) < (size / 2 - step)) {
                        continue;
                    }
                    int c = colorMap[index];
                    int abgr = (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c & 0xFF) << 16);

                    image.setPixelRGBA(tx, tz - 1, abgr);
                    index++;
                }
                tz = 0;
                tx ++;
            }


            dynamicTexture.upload();
            ResourceLocation texture = Minecraft.getInstance().getTextureManager().register("farplane_" + lodIndex, dynamicTexture);

            // Make mesh
            index = 0;
            int quads_count = 0;

            Map<Vertex, Integer> vertices = new HashMap<>();
            Map<Vertex, Vec3> normalAccumulator = new HashMap<>();
            Map<Vertex, Integer> vertexRefCount = new HashMap<>();


            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

            for (int x = -size; x < size; x += step) {
                for (int z = -size; z < size; z += step) {
                    if (Math.abs(x) < (size / 2 - step) && Math.abs(z) < (size / 2 - step)) {
                        continue;
                    }
                    if (index >= arraySize) break;

                    int worldX = centerX + x;
                    int worldZ = centerZ + z;
                    int worldY = heightMap[index];


                    Vertex v0 = new Vertex(worldX, worldZ);
                    Vertex v1 = new Vertex(worldX - step, worldZ);
                    Vertex v2 = new Vertex(worldX, worldZ - step);
                    Vertex v3 = new Vertex(worldX - step, worldZ - step);

                    Integer y1 = vertices.get(v1);
                    Integer y2 = vertices.get(v2);
                    Integer y3 = vertices.get(v3);

                    vertices.put(v0, worldY);

                    if (y1 != null && y2 != null && y3 != null) {

                        // Triangle 1: v0 - v1 - v2
                        Vec3 p0 = new Vec3(worldX, worldY, worldZ);
                        Vec3 p1 = new Vec3(worldX - step, y1, worldZ);
                        Vec3 p2 = new Vec3(worldX, y2, worldZ - step);

                        Vec3 edge1 = p1.subtract(p0);
                        Vec3 edge2 = p2.subtract(p0);
                        Vec3 normal1 = edge1.cross(edge2).normalize();

                        addNormal(normalAccumulator, vertexRefCount, v0, normal1);
                        addNormal(normalAccumulator, vertexRefCount, v1, normal1);
                        addNormal(normalAccumulator, vertexRefCount, v2, normal1);

                        // Triangle 2: v1 - v3 - v2
                        Vec3 p3 = new Vec3(worldX - step, y3, worldZ - step);
                        Vec3 edge3 = p3.subtract(p1);
                        Vec3 edge4 = p2.subtract(p1);
                        Vec3 normal2 = edge3.cross(edge4).normalize();

                        addNormal(normalAccumulator, vertexRefCount, v1, normal2);
                        addNormal(normalAccumulator, vertexRefCount, v3, normal2);
                        addNormal(normalAccumulator, vertexRefCount, v2, normal2);

                        Vec3 n0 = getSmoothNormal(normalAccumulator, vertexRefCount, v0);
                        Vec3 n1 = getSmoothNormal(normalAccumulator, vertexRefCount, v1);
                        Vec3 n2 = getSmoothNormal(normalAccumulator, vertexRefCount, v2);
                        Vec3 n3 = getSmoothNormal(normalAccumulator, vertexRefCount, v3);

                        float tu0 = ((float) x / size + 1) / 2.f, tv0 = ((float) z / size + 1) / 2f;
                        float tu1 = ((float) (x - step) / size + 1) / 2f, tv1 = ((float) (z) / size + 1) / 2f;
                        float tu2 = ((float) (x) / size + 1) / 2f, tv2 = ((float) (z - step) / size + 1) / 2f;
                        float tu3 = ((float) (x - step) / size + 1) / 2f, tv3 = ((float) (z - step) / size + 1) / 2f;

                        // First triangle
                        bufferBuilder.vertex(worldX, worldY, worldZ)
                                .uv(tu0, tv0)
                                .color(0xFFFFFFFF)
                                .normal((float) -n0.x, (float) -n0.y, (float) -n0.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX - step, y1, worldZ)
                                .uv(tu1, tv1)
                                .color(0xFFFFFFFF)
                                .normal((float) -n1.x, (float) -n1.y, (float) -n1.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX, y2, worldZ - step)
                                .uv(tu2, tv2)
                                .color(0xFFFFFFFF)
                                .normal((float) -n2.x, (float) -n2.y, (float) -n2.z)
                                .endVertex();

                        // Second triangle
                        bufferBuilder.vertex(worldX - step, y1, worldZ)
                                .uv(tu1, tv1)
                                .color(0xFFFFFFFF)
                                .normal((float) -n1.x, (float) -n1.y, (float) -n1.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX - step, y3, worldZ - step)
                                .uv(tu3, tv3)
                                .color(0xFFFFFFFF)
                                .normal((float) -n3.x, (float) -n3.y, (float) -n3.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX, y2, worldZ - step)
                                .uv(tu2, tv2)
                                .color(0xFFFFFFFF)
                                .normal((float) -n2.x, (float) -n2.y, (float) -n2.z)
                                .endVertex();
                        quads_count++;
                    }


                    index += 1;
                }
            }

            if (quads_count > 0) {
                BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
                VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);

                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
                VertexBuffer.unbind();

                return new MeshData(vertexBuffer, texture);
            }
            return null;
        } catch (Exception e) {
            bufferBuilder.discard();
            WorldFarView.LOGGER.error("Failed to create mesh", e);
            return null;
        }
    }

    record Vertex(int x, int z) {
    }


    private static void addNormal(Map<Vertex, Vec3> accumulator, Map<Vertex, Integer> refCount, Vertex v, Vec3 normal) {
        accumulator.merge(v, normal, Vec3::add);
        refCount.merge(v, 1, Integer::sum);
    }

    private static Vec3 getSmoothNormal(Map<Vertex, Vec3> accumulator, Map<Vertex, Integer> refCount, Vertex v) {
        Vec3 raw = accumulator.getOrDefault(v, new Vec3(0, 1, 0));
        int count = refCount.getOrDefault(v, 1);
        return raw.scale(1.0 / count).normalize();
    }

    public int[] getHeightMap() {
        return heightMap;
    }

    public void setHeightMap(int[] heightMap) {
        this.heightMap = heightMap;
    }

    public int[] getColorMap() {
        return colorMap;
    }

    public void setColorMap(int[] colorMap) {
        this.colorMap = colorMap;
    }

    public int getLodIndex() {
        return lodIndex;
    }

    public int getSize() {
        return size;
    }

    public int getResolution() {
        return resolution;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void close(){
        if (cachedMesh != null) {
            cachedMesh.close();
        }
    }
}