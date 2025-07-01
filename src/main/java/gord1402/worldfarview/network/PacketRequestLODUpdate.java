package gord1402.worldfarview.network;

import gord1402.worldfarview.config.ServerConfig;
import gord1402.worldfarview.server.FarPlaneManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketRequestLODUpdate(int lodIndex, int centerX, int centerZ, int resolution, int size) {
    public static void encode(PacketRequestLODUpdate pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.lodIndex);
        buf.writeInt(pkt.centerX);
        buf.writeInt(pkt.centerZ);
        buf.writeInt(pkt.resolution);
        buf.writeInt(pkt.size);
    }

    public static PacketRequestLODUpdate decode(FriendlyByteBuf buf) {
        int lodIndex = buf.readInt();
        int centerX = buf.readInt();
        int centerZ = buf.readInt();
        int resolution = buf.readInt();
        int size = buf.readInt();

        return new PacketRequestLODUpdate(lodIndex, centerX, centerZ, resolution, size);
    }

    public static void handle(PacketRequestLODUpdate pkt, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        ServerLevel level = player.serverLevel();

        if (ServerConfig.USE_CLIENT_SIDE_GENERATION.get()){
            ctx.get().enqueueWork(() -> {
                FarPlaneManager.sendServerConfigTo(player, level);
            });
        } else {
            FarPlaneManager.generate2Client(player, level, pkt.lodIndex, pkt.centerX, pkt.centerZ, pkt.resolution, pkt.size);
        }

        ctx.get().setPacketHandled(true);
    }
}
