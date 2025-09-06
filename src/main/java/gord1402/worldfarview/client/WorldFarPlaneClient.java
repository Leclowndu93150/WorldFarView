package gord1402.worldfarview.client;

import gord1402.worldfarview.config.Config;
import gord1402.worldfarview.FarPlaneLOD;
import gord1402.worldfarview.network.*;
import gord1402.worldfarview.registry.FarChunkGenerators;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@OnlyIn(Dist.CLIENT)
public class WorldFarPlaneClient {
    private static int START_SIZE = 1024;

    private static int NUM_LODS = 3;
    private static int RESOLUTION = 20;
    private static FarPlaneLOD[] lods = new FarPlaneLOD[NUM_LODS];

    private static final Deque<Integer> lodsUpdateQueue = new ArrayDeque<>();
    private static final Deque<Component> messagesQueue = new ArrayDeque<>();

    private static int duringUpdate = -1;
    private static int tickTimer = 0;

    private static boolean clientSideGeneration = false;
    private static ClientFarChunkGenerator clientChunkGenerator;
    private static long clientSeed;
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static boolean disabled = true;

    public static boolean configDirty = false;

    public static void loadConfig(){
        START_SIZE = Config.LOD0_START_SIZE.get();
        NUM_LODS = Config.NUM_LODS.get();
        RESOLUTION = Config.RESOLUTION.get();
        clear();
        lods = new FarPlaneLOD[NUM_LODS];

        disabled = false;
        if (Minecraft.getInstance().getConnection() != null){
            ModNetworking.CHANNEL.sendToServer(new PacketRequestServerConfiguration());
        }
    }

    public static void onServerConfig(PacketServerConfiguration pkt){
        clientSideGeneration = pkt.clientSideGeneration();
        if (clientSideGeneration){
            SerializableFarChunkGenerator<ClientFarChunkGenerator> farChunkGenerator = FarChunkGenerators.getClient(pkt.chunkGeneratorType);
            if (farChunkGenerator == null){
                messagesQueue.add(Component.literal("Client side generation is turned on on server side, but client doesn't support this chunk generator!").withStyle(ChatFormatting.RED));
                messagesQueue.add(Component.literal("Probably " + pkt.chunkGeneratorType.toString() + " not support WorldFarView client side generation.").withStyle(ChatFormatting.GOLD));

                processMessages();

                disabled = true;
            }
            else
            {
                clientChunkGenerator = farChunkGenerator.readNBT(pkt.configData);
                clientSeed = pkt.seed;
                disabled = false;
            }
        }
    }

    public static void generationError(Component message){
        messagesQueue.add(Component.literal("Server responded with error message:"));
        messagesQueue.add(message);
        messagesQueue.add(Component.literal("Client disabled. Fix error to enable it back (Open and close WorldFarView config screen to try again)."));

        processMessages();

        disabled = true;
        duringUpdate = -1;
    }

    public static void clear() {
        for (FarPlaneLOD lod: lods) {
            if (lod == null) continue;
            lod.close();
        }
    }

    private static void processMessages(){
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            while (!messagesQueue.isEmpty()){
                minecraft.player.displayClientMessage(messagesQueue.pop(), false);
            }
        }
    }


    public static void initLODs() {
        lodsUpdateQueue.clear();
        for (int i = 0; i < NUM_LODS; i++) {
            int size = (int)Math.pow(2, i) * START_SIZE;
            if (lods[i] != null) lods[i].close();
            lods[i] = new FarPlaneLOD(i, RESOLUTION, size);
            lodsUpdateQueue.add(i);
        }
    }

    public static void requestLODUpdate(double playerX, double playerZ){
        if (lodsUpdateQueue.isEmpty() || disabled) return;
        if (clientSideGeneration && clientChunkGenerator == null){
            disabled = true;
            return;
        }
        int index = lodsUpdateQueue.pop();
        FarPlaneLOD lod = lods[index];

        if (!clientSideGeneration) {
            ModNetworking.CHANNEL.sendToServer(new PacketRequestLODUpdate(lod.getLodIndex(), (int) playerX, (int) playerZ, lod.getResolution(), lod.getSize()));
        }
        else{
            executor.submit(() -> {
                    PacketLODUpdate pkt = FarPlaneLOD.generateOnClient(clientChunkGenerator, clientSeed, lod.getLodIndex(), (int) playerX, (int) playerZ, lod.getResolution(), lod.getSize());
                    WorldFarPlaneClient.updateLOD(pkt.lodIndex(), pkt.centerX(), pkt.centerZ(), pkt.resolution(), pkt.size(), pkt.heightMap(), pkt.colorMap());
            });
        }
        duringUpdate = index;
    }


    public static void updateLOD(int lodIndex, int centerX, int centerZ, int resolution, int size, int[] heightMapFlat, int[] colorMap) {
        if (lodIndex == duringUpdate) {
            duringUpdate = -1;
        }
        lods[lodIndex].setHeightMap(heightMapFlat);
        lods[lodIndex].setColorMap(colorMap);
        lods[lodIndex].centerX = centerX;
        lods[lodIndex].centerZ = centerZ;
        lods[lodIndex].markDirty();

    }

    private static boolean updateTimer(){
        tickTimer ++;
        if (tickTimer >= 20){
            tickTimer = 0;
            return true;
        }
        return false;
    }

    public static void tick(double playerX, double playerZ) {
        // Update chunk render boundaries for smooth adaptation
        ChunkRenderTracker.updateBoundary();
        
        if (configDirty) {
            if (Minecraft.getInstance().getConnection() != null){
                ModNetworking.CHANNEL.sendToServer(new PacketRequestServerConfiguration());
                configDirty = false;
            }
        }
        if (!messagesQueue.isEmpty()) processMessages();
        if (duringUpdate != -1 || disabled) return;
        if (!updateTimer()) return;
        requestLODUpdate(playerX, playerZ);

        for (FarPlaneLOD lod : lods) {
            int cx = lod.getCenterX();
            int cz = lod.getCenterZ();
            int size = lod.getSize();

            if (Math.abs(playerX - cx) > (double) size / 4 || Math.abs(playerZ - cz) > (double) size / 4) {
                if (lodsUpdateQueue.isEmpty()) lodsUpdateQueue.add(lod.getLodIndex());
            }
        }
    }


    public static FarPlaneLOD[] getLods() {
        return lods;
    }
}
