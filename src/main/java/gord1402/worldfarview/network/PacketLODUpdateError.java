package gord1402.worldfarview.network;

import gord1402.worldfarview.client.WorldFarPlaneClient;
import gord1402.worldfarview.server.FarPlaneManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketLODUpdateError(Component message) {
    public static void encode(PacketLODUpdateError pkt, FriendlyByteBuf buf) {
        buf.writeComponent(pkt.message);
    }

    public static PacketLODUpdateError decode(FriendlyByteBuf buf) {
        return new PacketLODUpdateError(buf.readComponent());
    }

    public static void handle(PacketLODUpdateError pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            WorldFarPlaneClient.generationError(pkt.message);
        });

        ctx.get().setPacketHandled(true);
    }
}
