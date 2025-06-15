package gord1402.worldfarview.network;

import gord1402.worldfarview.WorldFarView;
import gord1402.worldfarview.client.WorldFarPlaneClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketLODUpdate(int lodIndex, int centerX, int centerZ, int resolution, int size, int[] heightMap, int[] colorMap) {

    public static void encode(PacketLODUpdate pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.lodIndex);
        buf.writeInt(pkt.centerX);
        buf.writeInt(pkt.centerZ);
        buf.writeInt(pkt.resolution);
        buf.writeInt(pkt.size);

        // Write array length and content
        buf.writeInt(pkt.heightMap.length);
        for (int h : pkt.heightMap) {
            buf.writeInt(h);
        }

        for (int h : pkt.colorMap) {
            buf.writeInt(h);
        }
    }

    public static PacketLODUpdate decode(FriendlyByteBuf buf) {
        int lodIndex = buf.readInt();
        int centerX = buf.readInt();
        int centerZ = buf.readInt();
        int resolution = buf.readInt();
        int size = buf.readInt();

        int length = buf.readInt();
        int[] heightMap = new int[length];
        for (int i = 0; i < length; i++) {
            heightMap[i] = buf.readInt();
        }

        int[] colorMap = new int[length];
        for (int i = 0; i < length; i++) {
            colorMap[i] = buf.readInt();
        }

        return new PacketLODUpdate(lodIndex, centerX, centerZ, resolution, size, heightMap, colorMap);
    }

    public static void handle(PacketLODUpdate pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            WorldFarPlaneClient.updateLOD(pkt.lodIndex, pkt.centerX, pkt.centerZ, pkt.resolution, pkt.size, pkt.heightMap, pkt.colorMap);
        });
        ctx.get().setPacketHandled(true);
    }
}