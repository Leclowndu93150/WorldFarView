package gord1402.worldfarview.network;

import gord1402.worldfarview.WorldFarView;
import gord1402.worldfarview.server.FarPlaneManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketRequestServerConfiguration(){
    public static void encode(PacketRequestServerConfiguration pkt, FriendlyByteBuf buf) {
    }

    public static PacketRequestServerConfiguration decode(FriendlyByteBuf buf) {
        return new PacketRequestServerConfiguration();
    }

    public static void handle(PacketRequestServerConfiguration pkt, Supplier<NetworkEvent.Context> ctx) {

        ServerPlayer player = ctx.get().getSender();
        ServerLevel level = player.serverLevel();

        ctx.get().enqueueWork(() -> {
            FarPlaneManager.sendServerConfigTo(player, level);
        });

        ctx.get().setPacketHandled(true);
    }
}
