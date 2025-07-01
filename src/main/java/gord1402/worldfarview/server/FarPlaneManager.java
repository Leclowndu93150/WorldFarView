package gord1402.worldfarview.server;

import gord1402.worldfarview.FarPlaneLOD;
import gord1402.worldfarview.WorldFarView;
import gord1402.worldfarview.config.ServerConfig;
import gord1402.worldfarview.network.ModNetworking;
import gord1402.worldfarview.network.PacketLODUpdateError;
import gord1402.worldfarview.network.PacketServerConfiguration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FarPlaneManager {
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    public static int generate2Client(ServerPlayer player, ServerLevel level, int lodIndex, int centerX, int centerZ, int resolution, int size){
//        WorldFarView.LOGGER.info("Start loading LOD {}", lodIndex);
        if (lodIndex > ServerConfig.SERVER_MAX_LOD_LEVEL.get()) return sendErrorTo(player, Component.literal("LOD level exceeds server maximum (" + ServerConfig.SERVER_MAX_LOD_LEVEL.get() + ")"));
        if (resolution > ServerConfig.SERVER_MAX_RESOLUTION.get()) return sendErrorTo(player, Component.literal("Resolution exceeds server maximum (" + ServerConfig.SERVER_MAX_RESOLUTION.get() + ")"));
        executor.submit(() -> {
            try {
                FarPlaneLOD.generateOnServer(player, level, lodIndex, centerX, centerZ, resolution, size);
            } catch (Throwable t) {
                WorldFarView.LOGGER.error("Error generating LOD {}", lodIndex, t);
                sendErrorTo(player, Component.literal("Unexpected error during generation!"));
            }
        });
        return 0;
    }

    public static void sendServerConfigTo(ServerPlayer player, ServerLevel level){
        PacketServerConfiguration pkt = new PacketServerConfiguration(ServerConfig.USE_CLIENT_SIDE_GENERATION.get(), level.getChunkSource().getGenerator(), level.getSeed());
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), pkt);
    }

    public static int sendErrorTo(ServerPlayer player, Component message){
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketLODUpdateError(message));
        return 0;
    }
}
