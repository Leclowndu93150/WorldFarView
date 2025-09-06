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

    // Smooth adaptation settings
    public static final ForgeConfigSpec.BooleanValue SMOOTH_ADAPTATION;
    public static final ForgeConfigSpec.DoubleValue ADAPTATION_SPEED;
    public static final ForgeConfigSpec.IntValue MIN_BOUNDARY;
    public static final ForgeConfigSpec.IntValue MAX_BOUNDARY;

    // Static config values for easy access
    public static boolean disableVanillaFog = true;
    public static double customFogStart = 0.8;
    public static double customFogEnd = 1.5;
    public static int meshStartDistance = 0;
    public static boolean autoFitRenderDistance = true;
    public static boolean enableCrossfade = true;
    public static double crossfadeDistance = 32.0;
    public static boolean smoothAdaptation = false;
    public static double adaptationSpeed = 0.05;
    public static int minBoundary = 256;
    public static int maxBoundary = 2048;

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
        BUILDER.push("Smooth Adaptation");

        SMOOTH_ADAPTATION = BUILDER
                .comment("Enable smooth LOD adaptation based on actually rendered chunks")
                .define("smoothAdaptation", false);

        ADAPTATION_SPEED = BUILDER
                .comment("Speed of LOD boundary adaptation (0.01 = slow, 0.1 = fast)")
                .defineInRange("adaptationSpeed", 0.05, 0.01, 0.2);

        MIN_BOUNDARY = BUILDER
                .comment("Minimum LOD boundary distance in blocks")
                .defineInRange("minBoundary", 256, 128, 1024);

        MAX_BOUNDARY = BUILDER
                .comment("Maximum LOD boundary distance in blocks")
                .defineInRange("maxBoundary", 2048, 512, 4096);

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
        smoothAdaptation = SMOOTH_ADAPTATION.get();
        adaptationSpeed = ADAPTATION_SPEED.get();
        minBoundary = MIN_BOUNDARY.get();
        maxBoundary = MAX_BOUNDARY.get();
        
        // Update tracker settings if needed
        if (smoothAdaptation) {
            gord1402.worldfarview.client.ChunkRenderTracker.forceUpdate();
        }
    }


    public static void update() {
        WorldFarPlaneClient.loadConfig();
        WorldFarPlaneClient.initLODs();
    }
}