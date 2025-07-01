package gord1402.worldfarview;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

public abstract class FarChunkGenerator {
    public FarChunkGenerator() {

    }
    public abstract void init(ChunkGenerator generator, ServerLevel level);
    public abstract int getHeightAt(int x, int z);
    public abstract int getHexColorAt(int x, int y, int z);

    public abstract int getHexColorAt(int x, int z);
}
