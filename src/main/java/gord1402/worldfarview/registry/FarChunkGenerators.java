package gord1402.worldfarview.registry;

import com.mojang.serialization.Codec;
import gord1402.worldfarview.FarChunkGenerator;
import gord1402.worldfarview.client.ClientFarChunkGenerator;
import gord1402.worldfarview.network.SerializableFarChunkGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FarChunkGenerators {
    public static Map<Class<? extends ChunkGenerator>, Supplier<FarChunkGenerator>> SERVER_FAR_GENERATORS = new HashMap<>();
    public static Map<ResourceLocation, SerializableFarChunkGenerator<ClientFarChunkGenerator>> CLIENT_FAR_GENERATORS = new HashMap<>();

    public static Supplier<FarChunkGenerator> registerServer(Class<? extends ChunkGenerator> chunkGenerator, Supplier<FarChunkGenerator> farChunkGenerator){
        SERVER_FAR_GENERATORS.put(chunkGenerator, farChunkGenerator);
        return farChunkGenerator;
    }

    public static FarChunkGenerator getServer(ChunkGenerator chunkGenerator){
        return SERVER_FAR_GENERATORS.get(chunkGenerator.getClass()).get();
    }

    public static SerializableFarChunkGenerator<ClientFarChunkGenerator> registerClient(Codec<? extends ChunkGenerator> chunkGenerator, SerializableFarChunkGenerator<ClientFarChunkGenerator> farChunkGenerator){
        CLIENT_FAR_GENERATORS.put(BuiltInRegistries.CHUNK_GENERATOR.getKey(chunkGenerator), farChunkGenerator);
        return farChunkGenerator;
    }

    public static SerializableFarChunkGenerator<ClientFarChunkGenerator> getClient(ResourceLocation location){
        return CLIENT_FAR_GENERATORS.get(location);
    }

    public static Supplier<FarChunkGenerator> NOISE_BASED = registerServer(NoiseBasedChunkGenerator.class, VanilaFarChunkGenerator::new);
}
