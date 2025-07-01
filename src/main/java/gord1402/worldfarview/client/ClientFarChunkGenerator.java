package gord1402.worldfarview.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.ChunkGenerator;

public abstract class ClientFarChunkGenerator {
    public ClientFarChunkGenerator() {

    }

    public static void writeNBT(ChunkGenerator generator, FriendlyByteBuf buf){

    }

    public static ClientFarChunkGenerator readNBT(FriendlyByteBuf buf){
        return null;
    }

    public abstract void init(long seed);
    public abstract int getHeightAt(int x, int z);
    public abstract int getHexColorAt(int x, int y, int z);

    public abstract int getHexColorAt(int x, int z);
}
