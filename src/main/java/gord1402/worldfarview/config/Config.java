package gord1402.worldfarview.config;

import gord1402.worldfarview.WorldFarView;
import gord1402.worldfarview.client.WorldFarPlaneClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = WorldFarView.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Client Config
    public static final ForgeConfigSpec.IntValue LOD0_START_SIZE;
    public static final int LOD0_START_SIZE_MIN = 128;
    public static final int LOD0_START_SIZE_MAX = 8192;

    public static final ForgeConfigSpec.IntValue NUM_LODS;
    public static final int NUM_LODS_MIN = 1;
    public static final int NUM_LODS_MAX = 30;

    public static final ForgeConfigSpec.IntValue RESOLUTION;
    public static final int RESOLUTION_MIN = 1;
    public static final int RESOLUTION_MAX = 500;

    // New fog settings
    public static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_FOG;
    public static final ForgeConfigSpec.DoubleValue CUSTOM_FOG_START;
    public static final ForgeConfigSpec.DoubleValue CUSTOM_FOG_END;

    // New mesh settings  
    public static final ForgeConfigSpec.IntValue MESH_START_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue AUTO_FIT_RENDER_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CROSSFADE;
    public static final ForgeConfigSpec.DoubleValue CROSSFADE_DISTANCE;

    // Static config values for easy access
    public static boolean disableVanillaFog = true;
    public static double customFogStart = 0.8;
    public static double customFogEnd = 1.5;
    public static int meshStartDistance = 0;
    public static boolean autoFitRenderDistance = true;
    public static boolean enableCrossfade = true;
    public static double crossfadeDistance = 32.0;

    static {
        BUILDER.push("Client Settings");

        LOD0_START_SIZE = BUILDER
                .comment("The starting size for LOD0 (default: 1024)")
                .defineInRange("lod0StartSize", 1024, LOD0_START_SIZE_MIN, LOD0_START_SIZE_MAX);

        NUM_LODS = BUILDER
                .comment("Number of LOD levels (default: 3)")
                .defineInRange("numLods", 3, NUM_LODS_MIN, NUM_LODS_MAX);

        RESOLUTION = BUILDER
                .comment("Resolution parameter (default: 20)")
                .defineInRange("resolution", 20, RESOLUTION_MIN, RESOLUTION_MAX);

        BUILDER.pop();
        BUILDER.push("Fog Settings");

        DISABLE_VANILLA_FOG = BUILDER
                .comment("Disable vanilla fog rendering")
                .define("disableVanillaFog", true);

        CUSTOM_FOG_START = BUILDER
                .comment("Custom fog start multiplier for LOD terrain (default: 0.8)")
                .defineInRange("customFogStart", 0.8, 0.1, 2.0);

        CUSTOM_FOG_END = BUILDER
                .comment("Custom fog end multiplier for LOD terrain (default: 1.5)")
                .defineInRange("customFogEnd", 1.5, 1.0, 10.0);

        BUILDER.pop();
        BUILDER.push("Mesh Settings");

        MESH_START_DISTANCE = BUILDER
                .comment("Distance in blocks where LOD mesh starts (0 = auto-fit to render distance)")
                .defineInRange("meshStartDistance", 0, 0, 2048);

        AUTO_FIT_RENDER_DISTANCE = BUILDER
                .comment("Automatically fit LOD mesh to render distance")
                .define("autoFitRenderDistance", true);

        ENABLE_CROSSFADE = BUILDER
                .comment("Enable crossfade between real chunks and LOD mesh")
                .define("enableCrossfade", true);

        CROSSFADE_DISTANCE = BUILDER
                .comment("Distance in blocks for crossfade transition")
                .defineInRange("crossfadeDistance", 32.0, 8.0, 128.0);

        BUILDER.pop();

    }

    public static ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        disableVanillaFog = DISABLE_VANILLA_FOG.get();
        customFogStart = CUSTOM_FOG_START.get();
        customFogEnd = CUSTOM_FOG_END.get();
        meshStartDistance = MESH_START_DISTANCE.get();
        autoFitRenderDistance = AUTO_FIT_RENDER_DISTANCE.get();
        enableCrossfade = ENABLE_CROSSFADE.get();
        crossfadeDistance = CROSSFADE_DISTANCE.get();
    }


    public static void update() {
        WorldFarPlaneClient.loadConfig();
        WorldFarPlaneClient.initLODs();
    }
}