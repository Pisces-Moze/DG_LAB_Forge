package online.kbpf.dg_lab.client.screen.WaveformScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.screen.ButtonWidget;
import online.kbpf.dg_lab.client.screen.ConfigScreen;

import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_HEIGHT;
import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_SPACING;

public class WaveformConfigScreen extends Screen {

    private WaveformListWidget waveformListWidget;

    public WaveformConfigScreen() {
        super(new TextComponent("波形配置"));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ConfigScreen());
    }

    @Override
    protected void init() {
        int listTop = 40;
        int availableHeight = this.height - listTop - BUTTON_HEIGHT - (BUTTON_SPACING * 3);
        int listBottom = listTop + availableHeight;

        waveformListWidget = new WaveformListWidget(this.minecraft, this.width, availableHeight, listTop, listBottom, BUTTON_HEIGHT + BUTTON_SPACING);
        waveformListWidget.addWaveformEntry(new WaveformListWidget.Entry(this.font, new TextComponent("通道A - 伤害波形"), "ADamage"));
        waveformListWidget.addWaveformEntry(new WaveformListWidget.Entry(this.font, new TextComponent("通道A - 治疗波形"), "AHealing"));
        waveformListWidget.addWaveformEntry(new WaveformListWidget.Entry(this.font, new TextComponent("通道B - 伤害波形"), "BDamage"));
        waveformListWidget.addWaveformEntry(new WaveformListWidget.Entry(this.font, new TextComponent("通道B - 治疗波形"), "BHealing"));
        addRenderableWidget(waveformListWidget);

        ButtonWidget backButton = ButtonWidget.builder(new TextComponent("返回配置"), button -> this.minecraft.setScreen(new ConfigScreen()))
                .dimensions(this.width / 2 - 45, listBottom + BUTTON_SPACING, 90, BUTTON_HEIGHT)
                .build();
        addRenderableWidget(backButton);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font,
                "编辑默认波形。记得之后点击\"保存配置\"。",
                this.width / 2, 20, 0xFFFFFF);
    }
}
