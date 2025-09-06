package gord1402.worldfarview.client;

import gord1402.worldfarview.config.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class WorldFarViewConfigScreen extends Screen {
    private final Screen parentScreen;
    private final ForgeConfigSpec configSpec;
    private ConfigValueList entries;
    private Button doneButton;

    public WorldFarViewConfigScreen(Screen parent) {
        super(Component.literal("World Far View Configuration"));
        this.parentScreen = parent;
        this.configSpec = Config.SPEC;
    }

    @Override
    protected void init() {
        super.init();

        this.entries = new ConfigValueList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.addRenderableWidget(this.entries);

        entries.addConfigValue(new ConfigValueList.IntegerConfigValue(
                Component.literal("LOD0 Start Size"),
                Config.LOD0_START_SIZE,
                Config.LOD0_START_SIZE_MIN, Config.LOD0_START_SIZE_MAX
        ));

        entries.addConfigValue(new ConfigValueList.IntegerConfigValue(
                Component.literal("Number of LODs"),
                Config.NUM_LODS,
                Config.NUM_LODS_MIN, Config.NUM_LODS_MAX
        ));

        entries.addConfigValue(new ConfigValueList.IntegerConfigValue(
                Component.literal("Resolution Quality"),
                Config.RESOLUTION,
                Config.RESOLUTION_MIN, Config.RESOLUTION_MAX
        ));

        // Fog Settings
        entries.addConfigValue(new ConfigValueList.BooleanConfigValue(
                Component.literal("Disable Vanilla Fog"),
                Config.DISABLE_VANILLA_FOG
        ));

        entries.addConfigValue(new ConfigValueList.DoubleConfigValue(
                Component.literal("Custom Fog Start"),
                Config.CUSTOM_FOG_START,
                0.1, 2.0
        ));

        entries.addConfigValue(new ConfigValueList.DoubleConfigValue(
                Component.literal("Custom Fog End"),
                Config.CUSTOM_FOG_END,
                1.0, 10.0
        ));

        // Mesh Settings
        entries.addConfigValue(new ConfigValueList.IntegerConfigValue(
                Component.literal("Mesh Start Distance"),
                Config.MESH_START_DISTANCE,
                0, 2048
        ));

        entries.addConfigValue(new ConfigValueList.BooleanConfigValue(
                Component.literal("Auto Fit Render Distance"),
                Config.AUTO_FIT_RENDER_DISTANCE
        ));

        entries.addConfigValue(new ConfigValueList.BooleanConfigValue(
                Component.literal("Enable Crossfade"),
                Config.ENABLE_CROSSFADE
        ));

        entries.addConfigValue(new ConfigValueList.DoubleConfigValue(
                Component.literal("Crossfade Distance"),
                Config.CROSSFADE_DISTANCE,
                8.0, 128.0
        ));

        // Smooth Adaptation Settings
        entries.addConfigValue(new ConfigValueList.BooleanConfigValue(
                Component.literal("Smooth Adaptation"),
                Config.SMOOTH_ADAPTATION
        ));

        entries.addConfigValue(new ConfigValueList.DoubleConfigValue(
                Component.literal("Adaptation Speed"),
                Config.ADAPTATION_SPEED,
                0.01, 0.2
        ));

        entries.addConfigValue(new ConfigValueList.IntegerConfigValue(
                Component.literal("Min Boundary"),
                Config.MIN_BOUNDARY,
                128, 1024
        ));

        entries.addConfigValue(new ConfigValueList.IntegerConfigValue(
                Component.literal("Max Boundary"),
                Config.MAX_BOUNDARY,
                512, 4096
        ));

        this.doneButton = this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                button -> this.onClose()
        ).bounds(
                this.width / 2 - 100,
                this.height - 26,
                200,
                20
        ).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        this.entries.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                15,
                0xFFFFFF
        );
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
        Config.update();
    }
}