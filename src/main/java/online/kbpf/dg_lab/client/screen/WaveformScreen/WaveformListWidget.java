package online.kbpf.dg_lab.client.screen.WaveformScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.DgLabClient;
import online.kbpf.dg_lab.client.Tool.DGWaveformTool;
import online.kbpf.dg_lab.client.entity.Waveform.Waveform;
import online.kbpf.dg_lab.client.screen.ButtonWidget;
import online.kbpf.dg_lab.client.screen.TextFieldWidget;
import online.kbpf.dg_lab.client.screen.WaveformScreen.Custom.CustomScreen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_HEIGHT;
import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_SPACING;

/**
 * Forge port of the waveform editor list used to manage the four default waveform slots.
 */
public class WaveformListWidget extends ContainerObjectSelectionList<WaveformListWidget.Entry> {

    private final int listWidth;

    public WaveformListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.listWidth = width;
        this.x0 = 0;
        this.x1 = this.x0 + width;
        this.setRenderSelection(false);
    }

    public void addWaveformEntry(Entry entry) {
        this.addEntry(entry);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private static final int BUTTON_SIZE = 15;

        private final Minecraft minecraft = Minecraft.getInstance();
        private final Font font = minecraft.font;
        private final Component label;
        private final String waveformKey;
        private final int testChannel;

        private final TextFieldWidget waveformDataText;
        private final ButtonWidget customButton;
        private final ButtonWidget testButton;

        private final Waveform waveform;

        public Entry(Font font, Component label, String waveformKey) {
            this.label = label;
            this.waveformKey = waveformKey;

            Map<String, Waveform> waveformMap = DgLabClient.waveformMap;
            Waveform existing = waveformMap.get(waveformKey);
            if (existing == null) {
                existing = new Waveform();
                waveformMap.put(waveformKey, existing);
            }
            this.waveform = existing;

            this.waveformDataText = new TextFieldWidget(font, 0, 0, 100, BUTTON_HEIGHT - 4, TextComponent.EMPTY);
            this.waveformDataText.setMaxLength(100000);
            String initial = existing.getWaveform() == null ? "" : existing.getWaveform();
            this.waveformDataText.setValue(initial);
            this.waveformDataText.setResponder(waveform::setWaveform);

            this.testChannel = waveformKey.startsWith("B") ? 2 : 1;

            this.customButton = ButtonWidget.builder(new TextComponent("?"), button -> minecraft.setScreen(new CustomScreen(waveformKey)))
                    .size(BUTTON_SIZE, BUTTON_HEIGHT)
                    .tooltip(new TextComponent("Edit waveform control bars"))
                    .build();

            this.testButton = ButtonWidget.builder(new TextComponent("Send"), button -> {
                        if (DgLabClient.webSocketServer != null) {
                            DgLabClient.webSocketServer.sendDGWaveForm(waveformDataText.getValue(), testChannel);
                        }
                    })
                    .size(BUTTON_SIZE, BUTTON_HEIGHT)
                    .tooltip(new TextComponent("Send the current waveform to the device"))
                    .build();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return Arrays.asList(waveformDataText, customButton, testButton);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Arrays.asList(waveformDataText, customButton, testButton);
        }

        @Override
        public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int fieldX = x + (int) (entryWidth * 0.4f);
            int fieldWidth = (int) (entryWidth * 0.35f);
            waveformDataText.setPosition(fieldX, y + 2);
            waveformDataText.setWidth(fieldWidth);
            waveformDataText.setHeight(BUTTON_HEIGHT - 4);
            waveformDataText.render(poseStack, mouseX, mouseY, partialTick);

            customButton.setPosition(fieldX + fieldWidth + BUTTON_SPACING, y);
            customButton.setWidth(BUTTON_SIZE);
            customButton.setHeight(BUTTON_HEIGHT);
            customButton.render(poseStack, mouseX, mouseY, partialTick);

            testButton.setPosition(customButton.getX() + BUTTON_SIZE + BUTTON_SPACING, y);
            testButton.setWidth(BUTTON_SIZE);
            testButton.setHeight(BUTTON_HEIGHT);
            testButton.render(poseStack, mouseX, mouseY, partialTick);

            int labelX = x + (int) (entryWidth * 0.15f);
            font.drawShadow(poseStack, label, labelX, y + 5, 0xFFFFFF);

            int duration = DGWaveformTool.checkAndCountValidSubstrings(waveformDataText.getValue());
            Component durationText = duration <= 0
                    ? new TextComponent("ERROR")
                    : new TextComponent((duration * 100) + "ms");
            int colour = duration <= 0 ? 0xFF0000 : 0xFFFFFF;
            font.drawShadow(poseStack, durationText, testButton.getX() + BUTTON_SIZE + BUTTON_SPACING, y + 5, colour);
        }
    }
}



