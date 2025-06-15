package gord1402.worldfarview.network;

import gord1402.worldfarview.client.ClientFarChunkGenerator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.ChunkGenerator;

public interface SerializableFarChunkGenerator<T> {
    void writeNBT(ChunkGenerator generator, CompoundTag buf);

    T readNBT(CompoundTag buf);
}
