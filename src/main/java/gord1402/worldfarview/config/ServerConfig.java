package gord1402.worldfarview.config;

import gord1402.worldfarview.WorldFarView;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WorldFarView.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue USE_CLIENT_SIDE_GENERATION;
    public static final ForgeConfigSpec.IntValue SERVER_MAX_RESOLUTION;
    public static final ForgeConfigSpec.IntValue SERVER_MAX_LOD_LEVEL;

    static {
        BUILDER.push("Server Settings");

        USE_CLIENT_SIDE_GENERATION = BUILDER.comment("If true server will send information about generator and seed for client side world generation. Otherwise LOD's will be generated on server.")
                .define("clientSide", false);

        SERVER_MAX_RESOLUTION = BUILDER.comment("If server side generation it will cancel any generation request's that have more then this resolution.")
                .defineInRange("maxResolution", 400, 0, 1000);

        SERVER_MAX_LOD_LEVEL = BUILDER.comment("If server side generation it will cancel any generation request's that have more then this lod level.")
                .defineInRange("maxLevel", 30, 0, 30);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
