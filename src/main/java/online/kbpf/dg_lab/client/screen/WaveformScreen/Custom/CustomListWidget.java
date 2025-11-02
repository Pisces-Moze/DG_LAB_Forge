package online.kbpf.dg_lab.client.screen.WaveformScreen.Custom;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.entity.Waveform.ControlBar;
import online.kbpf.dg_lab.client.screen.ButtonWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * List widget that allows fine-grained editing of waveform control bars.
 */
public class CustomListWidget extends ContainerObjectSelectionList<CustomListWidget.Entry> {

    private final List<ControlBar> controlBars;

    public CustomListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight, List<ControlBar> controlBars) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.controlBars = controlBars;
        this.x0 = 0;
        this.setRenderSelection(false);
    }

    public void refreshEntries() {
        super.clearEntries();
        for (int i = 0; i < controlBars.size(); i++) {
            this.addEntry(new Entry(controlBars, i));
        }
    }

    public void removeLastEntry() {
        if (!controlBars.isEmpty()) {
            controlBars.remove(controlBars.size() - 1);
            refreshEntries();
        }
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private static final MutableComponent MANUAL = new TextComponent("Manual")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(true).withUnderlined(true));
        private static final MutableComponent AUTOMATIC = new TextComponent("Auto")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)));

        private final List<ControlBar> controlBars;
        private int index;

        private final ButtonWidget strengthToggle;
        private final ButtonWidget frequencyToggle;
        private final CustomSliderWidget strengthSlider;
        private final CustomSliderWidget frequencySlider;

        public Entry(List<ControlBar> controlBars, int index) {
            this.controlBars = controlBars;
            this.index = index;
            ControlBar controlBar = controlBars.get(index);

            this.strengthSlider = new CustomSliderWidget(0, 0, 100, 8, new TextComponent(String.valueOf(controlBar.getStrength())), controlBar.getStrength() * 0.01) {
                @Override
                protected void updateMessage() {
                    ControlBar bar = controlBars.get(Entry.this.index);
                    bar.setStrength((int) Math.round(this.value * 100));
                    controlBars.set(Entry.this.index, bar);
                    updateStrengthAnchors();
                }

                @Override
                protected void applyValue() {
                }
            };

            this.frequencySlider = new CustomSliderWidget(0, 0, 100, 8, new TextComponent(String.valueOf(controlBar.getFrequency())), controlBar.getFrequency() * 0.01) {
                @Override
                protected void updateMessage() {
                    // handled in applyValue to clamp minimum
                }

                @Override
                protected void applyValue() {
                    ControlBar bar = controlBars.get(Entry.this.index);
                    double clamped = Math.max(0.1, this.value);
                    this.value = clamped;
                    bar.setFrequency((int) Math.round(clamped * 100));
                    controlBars.set(Entry.this.index, bar);
                    updateFrequencyAnchors();
                }
            };

            this.strengthToggle = ButtonWidget.builder(controlBar.isS_on_off() ? MANUAL.copy() : AUTOMATIC.copy(), button -> {
                        ControlBar bar = controlBars.get(this.index);
                        bar.setS_on_off(!bar.isS_on_off());
                        button.setMessage(bar.isS_on_off() ? MANUAL.copy() : AUTOMATIC.copy());
                        controlBars.set(this.index, bar);
                        updateStrengthAnchors();
                        strengthSlider.active = bar.isS_on_off();
                    })
                    .size(22, 8)
                    .build();

            this.frequencyToggle = ButtonWidget.builder(controlBar.isF_on_off() ? MANUAL.copy() : AUTOMATIC.copy(), button -> {
                        ControlBar bar = controlBars.get(this.index);
                        bar.setF_on_off(!bar.isF_on_off());
                        button.setMessage(bar.isF_on_off() ? MANUAL.copy() : AUTOMATIC.copy());
                        controlBars.set(this.index, bar);
                        updateFrequencyAnchors();
                        frequencySlider.active = bar.isF_on_off();
                    })
                    .size(22, 8)
                    .build();

            this.strengthSlider.active = controlBar.isS_on_off();
            this.frequencySlider.active = controlBar.isF_on_off();

            updateStrengthAnchors();
            updateFrequencyAnchors();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            List<NarratableEntry> entries = new ArrayList<>();
            entries.add(frequencyToggle);
            entries.add(strengthToggle);
            if (frequencySlider.active) {
                entries.add(frequencySlider);
            }
            if (strengthSlider.active) {
                entries.add(strengthSlider);
            }
            return entries;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            List<GuiEventListener> listeners = new ArrayList<>();
            listeners.add(frequencyToggle);
            listeners.add(strengthToggle);
            if (frequencySlider.active) {
                listeners.add(frequencySlider);
            }
            if (strengthSlider.active) {
                listeners.add(strengthSlider);
            }
            return listeners;
        }

        @Override
        public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {
            this.index = index;

            int frequencyX = x + (int) (entryWidth * 0.015f);
            frequencyToggle.setPosition(frequencyX, y);

            frequencySlider.setPosition(frequencyToggle.getX() + frequencyToggle.getWidth() + 2, y);
            frequencySlider.setWidth((int) (entryWidth * 0.2f));

            strengthToggle.setPosition(frequencySlider.getX() + frequencySlider.getWidth() + 20, y);

            strengthSlider.setPosition(strengthToggle.getX() + strengthToggle.getWidth() + 2, y);
            strengthSlider.setWidth((int) (entryWidth * 0.6f));

            ControlBar bar = controlBars.get(index);
            strengthSlider.setValue(bar.getStrength());
            frequencySlider.setValue(bar.getFrequency());

            frequencyToggle.render(poseStack, mouseX, mouseY, partialTick);
            frequencySlider.render(poseStack, mouseX, mouseY, partialTick);
            strengthToggle.render(poseStack, mouseX, mouseY, partialTick);
            strengthSlider.render(poseStack, mouseX, mouseY, partialTick);
        }

        private void updateStrengthAnchors() {
            adjustInterpolatedValues(true);
        }

        private void updateFrequencyAnchors() {
            adjustInterpolatedValues(false);
        }

        private void adjustInterpolatedValues(boolean strength) {
            ControlBar bar = controlBars.get(index);
            boolean manual = strength ? bar.isS_on_off() : bar.isF_on_off();

            int left = findAnchor(index, strength, true);
            int right = findAnchor(index, strength, false);

            if (manual) {
                if (left < index) {
                    interpolateRange(left, index, strength);
                }
                if (right > index) {
                    interpolateRange(index, right, strength);
                }
            } else if (left < right) {
                interpolateRange(left, right, strength);
            }
        }

        private int findAnchor(int start, boolean strength, boolean backwards) {
            int i = start;
            while (true) {
                i += backwards ? -1 : 1;
                if (i < 0) {
                    return 0;
                }
                if (i >= controlBars.size()) {
                    return controlBars.size() - 1;
                }
                ControlBar bar = controlBars.get(i);
                boolean manual = strength ? bar.isS_on_off() : bar.isF_on_off();
                if (manual) {
                    return i;
                }
            }
        }

        private void interpolateRange(int minIndex, int maxIndex, boolean strength) {
            if (minIndex >= maxIndex) {
                return;
            }

            ControlBar minBar = controlBars.get(minIndex);
            ControlBar maxBar = controlBars.get(maxIndex);

            int minValue = strength ? minBar.getStrength() : minBar.getFrequency();
            int maxValue = strength ? maxBar.getStrength() : maxBar.getFrequency();
            double delta = (double) (maxValue - minValue) / (maxIndex - minIndex);

            for (int i = minIndex + 1; i < maxIndex; i++) {
                ControlBar bar = controlBars.get(i);
                if (strength && bar.isS_on_off()) {
                    continue;
                }
                if (!strength && bar.isF_on_off()) {
                    continue;
                }

                int interpolated = (int) Math.round(minValue + delta * (i - minIndex));
                if (strength) {
                    bar.setStrength(interpolated);
                } else {
                    bar.setFrequency(interpolated);
                }
                controlBars.set(i, bar);
            }
        }
    }
}

