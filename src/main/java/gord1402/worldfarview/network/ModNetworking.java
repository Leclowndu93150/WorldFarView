package gord1402.worldfarview.network;

import gord1402.worldfarview.WorldFarView;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WorldFarView.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++,
                PacketLODUpdate.class,
                PacketLODUpdate::encode,
                PacketLODUpdate::decode,
                PacketLODUpdate::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++,
                PacketRequestLODUpdate.class,
                PacketRequestLODUpdate::encode,
                PacketRequestLODUpdate::decode,
                PacketRequestLODUpdate::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++,
                PacketRequestServerConfiguration.class,
                PacketRequestServerConfiguration::encode,
                PacketRequestServerConfiguration::decode,
                PacketRequestServerConfiguration::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++,
                PacketServerConfiguration.class,
                PacketServerConfiguration::encode,
                PacketServerConfiguration::decode,
                PacketServerConfiguration::handle);

        CHANNEL.registerMessage(packetId++,
                PacketLODUpdateError.class,
                PacketLODUpdateError::encode,
                PacketLODUpdateError::decode,
                PacketLODUpdateError::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

    }
}