package gord1402.worldfarview.registry;

import com.mojang.blaze3d.platform.NativeImage;
import gord1402.worldfarview.FarChunkGenerator;
import gord1402.worldfarview.mixin.BlockStateBaseAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

public class VanilaFarChunkGenerator extends FarChunkGenerator {
    ServerLevel level;
    ChunkGenerator generator;
    RandomState randomState;
    LevelHeightAccessor accessor;
    DensityFunction densityFunction;
    NoiseGeneratorSettings settings;

    public VanilaFarChunkGenerator(){
        super();

    }

    @Override
    public void init(ChunkGenerator generator, ServerLevel level) {
        this.randomState = level.getChunkSource().randomState();
        this.accessor = level;
        this.level = level;
        this.generator = (NoiseBasedChunkGenerator) generator;
        this.settings = ((NoiseBasedChunkGenerator) generator).generatorSettings().value();
        this.densityFunction = randomState.router().finalDensity();
    }

    @Override
    public int getHeightAt(int x, int z) {
        int low = settings.seaLevel();
        int high = settings.seaLevel() + settings.noiseSettings().height();
        int result = low;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (sampleDensity(x, mid, z) > 0) {
                result = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    private double sampleDensity(int x, int y, int z) {
        return densityFunction.compute(new DensityFunction.SinglePointContext(x, y, z));
    }

    @Override
    public int getHexColorAt(int x, int y, int z) {
        Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(x),
                QuartPos.fromBlock(y),
                QuartPos.fromBlock(z),
                randomState.sampler()
        );

        ResourceLocation biomeId = biome.unwrapKey().orElseThrow().location();

        Climate.Sampler sampler = randomState.sampler();
        Climate.TargetPoint climate = sampler.sample(x, y, z);

        double temperature = climate.temperature();

        int color;

        if (y <= settings.seaLevel()){
            color = 0x4060D0;
        }
        else {
            color = switch (biomeId.getPath()) {
                case "desert" -> 0xEEDD88; // sand
                case "snowy_taiga", "snowy_plains", "snowy_slopes", "frozen_peaks" -> 0xFFFFFF; // snow
                case "windswept_hills", "windswept_gravelly_hills" -> 0x888888; // stone
                case "badlands", "eroded_badlands" -> 0xD27847; // terracotta
                case "savanna" -> 0xBDB76B; // dry grass
                default -> {
                    if (y > 150) yield 0xFFFFFF;
                    yield biome.value().getGrassColor(x, z);
                }
            };
        }

        return 0xFF000000 | color;
    }
}
