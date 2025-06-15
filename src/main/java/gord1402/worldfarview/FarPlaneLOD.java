package gord1402.worldfarview;

import com.mojang.blaze3d.vertex.*;
import gord1402.worldfarview.client.ClientFarChunkGenerator;
import gord1402.worldfarview.network.ModNetworking;
import gord1402.worldfarview.network.PacketLODUpdate;
import gord1402.worldfarview.registry.FarChunkGenerators;
import gord1402.worldfarview.renderer.MeshData;
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
            int index = 0;
            int quads_count = 0;

            Map<Vertex, Integer> vertices = new HashMap<>();
            Map<Vertex, Integer> vertices_colors = new HashMap<>();
            Map<Vertex, Vec3> normalAccumulator = new HashMap<>();
            Map<Vertex, Integer> vertexRefCount = new HashMap<>();


            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

            int step = size / resolution;

            for (int x = -size; x < size; x += step) {
                for (int z = -size; z < size; z += step) {
                    if (Math.abs(x) < (size / 2 - step) && Math.abs(z) < (size / 2 - step)) {
                        continue;
                    }
                    if (index >= arraySize) break;

                    int worldX = centerX + x;
                    int worldZ = centerZ + z;
                    int worldY = heightMap[index];

                    int color = colorMap[index];
                    int a = (color >> 24) & 0xFF, r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = (color) & 0xFF;


                    Vertex v0 = new Vertex(worldX, worldZ);
                    Vertex v1 = new Vertex(worldX - step, worldZ);
                    Vertex v2 = new Vertex(worldX, worldZ - step);
                    Vertex v3 = new Vertex(worldX - step, worldZ - step);

                    Integer y1 = vertices.get(v1);
                    Integer y2 = vertices.get(v2);
                    Integer y3 = vertices.get(v3);

                    vertices.put(v0, worldY);
                    vertices_colors.put(v0, color);

                    if (y1 != null && y2 != null && y3 != null) {
                        int color1 = vertices_colors.get(v1);
                        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = (color1) & 0xFF;

                        int color2 = vertices_colors.get(v2);
                        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = (color2) & 0xFF;

                        int color3 = vertices_colors.get(v3);
                        int a3 = (color3 >> 24) & 0xFF, r3 = (color3 >> 16) & 0xFF, g3 = (color3 >> 8) & 0xFF, b3 = (color3) & 0xFF;

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

                        // First triangle
                        bufferBuilder.vertex(worldX, worldY, worldZ)
                                .color(r, g, b, a)
                                .normal((float) -n0.x, (float) -n0.y, (float) -n0.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX - step, y1, worldZ)
                                .color(r1, g1, b1, a1)
                                .normal((float) -n1.x, (float) -n1.y, (float) -n1.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX, y2, worldZ - step)
                                .color(r2, g2, b2, a2)
                                .normal((float) -n2.x, (float) -n2.y, (float) -n2.z)
                                .endVertex();

                        // Second triangle
                        bufferBuilder.vertex(worldX - step, y1, worldZ)
                                .color(r1, g1, b1, a1)
                                .normal((float) -n1.x, (float) -n1.y, (float) -n1.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX - step, y3, worldZ - step)
                                .color(r3, g3, b3, a3)
                                .normal((float) -n3.x, (float) -n3.y, (float) -n3.z)
                                .endVertex();

                        bufferBuilder.vertex(worldX, y2, worldZ - step)
                                .color(r2, g2, b2, a2)
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

                return new MeshData(vertexBuffer);
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