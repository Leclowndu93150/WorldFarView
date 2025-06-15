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

    }

    public static ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }


    public static void update() {
        WorldFarPlaneClient.loadConfig();
        WorldFarPlaneClient.initLODs();
    }
}