package online.kbpf.dg_lab.client.screen.WaveformScreen.Custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.DgLabClient;
import online.kbpf.dg_lab.client.entity.Waveform.ControlBar;
import online.kbpf.dg_lab.client.entity.Waveform.Waveform;
import online.kbpf.dg_lab.client.screen.ButtonWidget;
import online.kbpf.dg_lab.client.screen.WaveformScreen.WaveformConfigScreen;

import java.util.ArrayList;
import java.util.List;

public class CustomScreen extends Screen {

    private static final int MAX_CONTROL_BARS = 348;
    private static final int ROW_HEIGHT = 8;

    private final String waveformKey;
    private final List<ControlBar> controlBars = new ArrayList<>();

    private CustomListWidget customListWidget;
    private ButtonWidget addButton;
    private ButtonWidget deleteButton;

    public CustomScreen(String waveformKey) {
        super(new TextComponent("Custom Waveform Editor"));
        this.waveformKey = waveformKey;

        Waveform waveform = DgLabClient.waveformMap.getOrDefault(waveformKey, new Waveform());
        this.controlBars.addAll(waveform.getList());
        if (this.controlBars.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                this.controlBars.add(new ControlBar());
            }
        }
    }

    @Override
    public void onClose() {
        Waveform waveform = DgLabClient.waveformMap.getOrDefault(waveformKey, new Waveform());
        waveform.setList(controlBars);
        waveform.graphToData();
        DgLabClient.waveformMap.put(waveformKey, waveform);
        this.minecraft.setScreen(new WaveformConfigScreen());
    }

    @Override
    protected void init() {
        Minecraft minecraft = Minecraft.getInstance();
        customListWidget = new CustomListWidget(minecraft, this.width, this.height - 40, 20, this.height - 20, ROW_HEIGHT, controlBars);
        customListWidget.refreshEntries();

        addButton = ButtonWidget.builder(new TextComponent(labelForAddButton()), button -> {
                    if (controlBars.size() < MAX_CONTROL_BARS) {
                        for (int i = 0; i < 4; i++) {
                            controlBars.add(new ControlBar());
                        }
                        customListWidget.refreshEntries();
                        button.setMessage(new TextComponent(labelForAddButton()));
                    }
                })
                .dimensions((int) (this.width * 0.1), this.height - 22, (int) (this.width * 0.7), 15)
                .tooltip(new TextComponent("Add four control bars at a time (maximum 348 entries)."))
                .build();

        deleteButton = ButtonWidget.builder(new TextComponent("Remove 4"), button -> {
                    if (controlBars.size() > 7) {
                        for (int i = 0; i < 4 && !controlBars.isEmpty(); i++) {
                            controlBars.remove(controlBars.size() - 1);
                        }
                        customListWidget.refreshEntries();
                        addButton.setMessage(new TextComponent(labelForAddButton()));
                    }
                })
                .dimensions((int) (this.width * 0.8), this.height - 22, (int) (this.width * 0.1), 15)
                .tooltip(new TextComponent("Remove the last four control bars."))
                .build();

        addRenderableWidget(addButton);
        addRenderableWidget(deleteButton);
        addRenderableWidget(customListWidget);
    }

    private String labelForAddButton() {
        return controlBars.size() >= MAX_CONTROL_BARS ? "--- MAX ---" : "Add 4";
    }
}
