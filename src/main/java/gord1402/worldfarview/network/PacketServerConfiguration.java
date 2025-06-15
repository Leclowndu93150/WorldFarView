package gord1402.worldfarview.network;

import gord1402.worldfarview.WorldFarView;
import gord1402.worldfarview.client.ClientFarChunkGenerator;
import gord1402.worldfarview.client.WorldFarPlaneClient;
import gord1402.worldfarview.mixin.ChunkGeneratorAccessor;
import gord1402.worldfarview.registry.FarChunkGenerators;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketServerConfiguration {
    public final boolean clientSideGeneration;

    public final CompoundTag configData;
    public final ResourceLocation chunkGeneratorType;
    public final Long seed;

    public PacketServerConfiguration(boolean clientSideGeneration, ChunkGenerator generator, Long seed) {
        this.clientSideGeneration = clientSideGeneration;
        if (clientSideGeneration){
            this.chunkGeneratorType = BuiltInRegistries.CHUNK_GENERATOR.getKey(((ChunkGeneratorAccessor) generator).callCodec());
            this.configData = new CompoundTag();
            SerializableFarChunkGenerator<ClientFarChunkGenerator> farChunkGenerator = FarChunkGenerators.getClient(chunkGeneratorType);
            if (farChunkGenerator == null){
                WorldFarView.LOGGER.error("Client side generation enabled for unsupported chunk generator!");
            }
            else
            {
                farChunkGenerator.writeNBT(generator, this.configData);
            }
            this.seed = seed;
        }
        else{
            configData = null;
            chunkGeneratorType = null;
            this.seed = null;
        }
    }

    public PacketServerConfiguration(boolean clientSideGeneration, CompoundTag configData, ResourceLocation chunkGeneratorType, Long seed){
        this.clientSideGeneration = clientSideGeneration;
        this.configData = configData;
        this.chunkGeneratorType = chunkGeneratorType;
        this.seed = seed;
    }



    public static void encode(PacketServerConfiguration pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.clientSideGeneration);
        if (pkt.clientSideGeneration){
            buf.writeResourceLocation(pkt.chunkGeneratorType);
            buf.writeNbt(pkt.configData);
            buf.writeLong(pkt.seed);
        }
    }

    public static PacketServerConfiguration decode(FriendlyByteBuf buf) {
        boolean clientSideGeneration = buf.readBoolean();
        if (clientSideGeneration){
            ResourceLocation chunkGeneratorType = buf.readResourceLocation();
            CompoundTag configData = buf.readNbt();
            long seed = buf.readLong();
            return new PacketServerConfiguration(true, configData, chunkGeneratorType, seed);
        }
        return new PacketServerConfiguration(false, null, null, null);
    }

    public static void handle(PacketServerConfiguration pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            WorldFarPlaneClient.onServerConfig(pkt);
        });
        ctx.get().setPacketHandled(true);
    }

    public boolean clientSideGeneration(){
        return clientSideGeneration;
    }
}
