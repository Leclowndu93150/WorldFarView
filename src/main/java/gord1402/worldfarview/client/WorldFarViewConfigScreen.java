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