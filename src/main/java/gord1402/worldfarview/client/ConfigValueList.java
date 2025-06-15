package gord1402.worldfarview.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ConfigValueList extends ContainerObjectSelectionList<ConfigValueList.ConfigValueEntry> {

    public ConfigValueList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    public void addConfigValue(AbstractConfigValue<?> configValue) {
        ConfigValueEntry entry = new ConfigValueEntry(configValue);
        configValue.setParentEntry(entry);
        this.addEntry(entry);
    }

    public static class ConfigValueEntry extends Entry<ConfigValueEntry> {
        private final AbstractConfigValue<?> configValue;
        private final List<GuiEventListener> children = new ArrayList<>();
        private final List<NarratableEntry> narratables = new ArrayList<>();

        public ConfigValueEntry(AbstractConfigValue<?> configValue) {
            this.configValue = configValue;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            this.configValue.render(guiGraphics, left, top, width, height, mouseX, mouseY, isMouseOver, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return narratables;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.configValue.onClick(mouseX, mouseY);
            return super.mouseClicked(mouseX, mouseY, button) ||
                    this.configValue.mouseClicked(mouseX, mouseY, button);
        }
    }

    public abstract static class AbstractConfigValue<T> {
        protected final Component label;
        protected final ForgeConfigSpec.ConfigValue<T> configValue;
        protected ConfigValueList.ConfigValueEntry parentEntry;

        public AbstractConfigValue(Component label, ForgeConfigSpec.ConfigValue<T> configValue) {
            this.label = label;
            this.configValue = configValue;
        }

        public void setParentEntry(ConfigValueList.ConfigValueEntry entry) {
            this.parentEntry = entry;
        }

        public abstract void render(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                    int mouseX, int mouseY, boolean isMouseOver, float partialTick);

        public abstract void onClick(double mouseX, double mouseY);

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }
    }


    public static class IntegerConfigValue extends AbstractConfigValue<Integer> {
        private final int min;
        private final int max;
        private EditBox textField;

        public IntegerConfigValue(Component label, ForgeConfigSpec.IntValue configValue, int min, int max) {
            super(label, configValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            if (textField == null) {
                textField = new EditBox(Minecraft.getInstance().font,
                        x + width - 100, y, 80, 20,
                        Component.literal(String.valueOf(configValue.get())));
                textField.setValue(String.valueOf(configValue.get()));
                textField.setResponder(value -> {
                    try {
                        int intValue = Integer.parseInt(value);
                        int clamped = Mth.clamp(intValue, min, max);
                        if (intValue == clamped) {
                            configValue.set(intValue);
                        }
                        else {
                            textField.setValue(String.valueOf(clamped));
                        }
                    } catch (NumberFormatException ignored) {}
                });

                if (parentEntry != null) {
                    parentEntry.children.add(textField);
                    parentEntry.narratables.add(textField);
                }
            }

            guiGraphics.drawString(Minecraft.getInstance().font,
                    label,
                    x + 10,
                    y + 6,
                    0xFFFFFF);

            textField.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (textField != null) {
                textField.mouseClicked(mouseX, mouseY, 0);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return textField != null && textField.mouseClicked(mouseX, mouseY, button);
        }
    }
}