package online.kbpf.dg_lab.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.Config.WaveformConfig;
import online.kbpf.dg_lab.client.createQR.ToolQR;
import online.kbpf.dg_lab.client.screen.StrengthScreen.StrengthConfigScreen;
import online.kbpf.dg_lab.client.screen.WaveformScreen.WaveformConfigScreen;

import static online.kbpf.dg_lab.client.DgLabClient.modConfig;
import static online.kbpf.dg_lab.client.DgLabClient.strengthConfig;
import static online.kbpf.dg_lab.client.DgLabClient.waveformMap;

public class ConfigScreen extends Screen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_SPACING = 5;

    private SliderWidget renderingPositionX;
    private SliderWidget renderingPositionY;

    public ConfigScreen() {
        super(new TextComponent("配置"));
    }

    @Override
    protected void init() {
        Minecraft minecraft = Minecraft.getInstance();
        int guiScaledWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiScaledHeight = minecraft.getWindow().getGuiScaledHeight();

        // 使用正确的屏幕尺寸
        int screenWidth = this.width;
        int screenHeight = this.height;

        int leftColumnX = (int) (screenWidth / 2.0 - (screenWidth * 0.4) - 5);
        int rightColumnX = screenWidth / 2 + 5;
        int sliderY = 140 - BUTTON_HEIGHT - BUTTON_SPACING;
        int sliderWidth = (int) (screenWidth * 0.2) - 6;

        renderingPositionX = createPositionSlider(true, rightColumnX, sliderY, sliderWidth, screenWidth, screenHeight);
        renderingPositionY = createPositionSlider(false, rightColumnX + sliderWidth, sliderY, sliderWidth, screenWidth, screenHeight);

        ButtonWidget maxStrength = ButtonWidget.builder(toggleMaxStrengthMessage(), button -> {
                    modConfig.setRenderingMax(!modConfig.isRenderingMax());
                    button.setMessage(toggleMaxStrengthMessage());
                })
                .dimensions(renderingPositionY.getX() + renderingPositionY.getWidth(), sliderY, 110, BUTTON_HEIGHT)
                .tooltip(new TextComponent("在HUD覆盖层中切换最大强度显示"))
                .build();

        ButtonWidget saveFile = ButtonWidget.builder(new TextComponent("保存配置"), button -> {
                    strengthConfig.savaFile();
                    modConfig.savaFile();
                    WaveformConfig.saveWaveform(waveformMap);
                })
                .dimensions(leftColumnX, 20, (int) (screenWidth * 0.4), BUTTON_HEIGHT)
                .tooltip(new TextComponent("将当前设置写入JSON配置文件"))
                .build();

        ButtonWidget webSocketConfig = ButtonWidget.builder(new TextComponent("WebSocket设置"),
                        button -> this.minecraft.setScreen(new WebSocketConfigScreen()))
                .dimensions(rightColumnX, 20, (int) (screenWidth * 0.4), BUTTON_HEIGHT)
                .tooltip(new TextComponent("编辑二维码主机和端口值"))
                .build();

        ButtonWidget strengthConfigButton = ButtonWidget.builder(new TextComponent("强度设置"),
                        button -> this.minecraft.setScreen(new StrengthConfigScreen()))
                .dimensions(leftColumnX, 20 + BUTTON_HEIGHT + BUTTON_SPACING, (int) (screenWidth * 0.4), BUTTON_HEIGHT)
                .tooltip(new TextComponent("调整伤害加成、延迟和冷却值"))
                .build();

        ButtonWidget waveFormConfig = ButtonWidget.builder(new TextComponent("波形设置"),
                        button -> this.minecraft.setScreen(new WaveformConfigScreen()))
                .dimensions(rightColumnX, 20 + BUTTON_HEIGHT + BUTTON_SPACING, (int) (screenWidth * 0.4), BUTTON_HEIGHT)
                .tooltip(new TextComponent("编辑发送到DG-LAB设备的波形"))
                .build();

        ButtonWidget createQR = ButtonWidget.builder(new TextComponent("创建二维码并打开"),
                        button -> ToolQR.CreateQR())
                .dimensions(leftColumnX, sliderY, (int) (screenWidth * 0.4), BUTTON_HEIGHT)
                .tooltip(new TextComponent("生成二维码图像并用系统查看器打开\n图像路径: " + System.getProperty("user.dir")))
                .build();

        addRenderableWidget(saveFile);
        addRenderableWidget(webSocketConfig);
        addRenderableWidget(strengthConfigButton);
        addRenderableWidget(waveFormConfig);
        addRenderableWidget(createQR);
        addRenderableWidget(renderingPositionX);
        addRenderableWidget(renderingPositionY);
        addRenderableWidget(maxStrength);
    }

    private SliderWidget createPositionSlider(boolean isX, int x, int y, int width, int screenWidth, int screenHeight) {
        int current = isX ? modConfig.getRenderingPositionX() : modConfig.getRenderingPositionY();
        int range = isX ? screenWidth : screenHeight;
        double value = range > 0 ? Math.min(1.0, Math.max(0.0, current / (double) range)) : 0.0;

        return new SliderWidget(x, y, width, BUTTON_HEIGHT,
                positionText(screenWidth, screenHeight, isX), value) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int pixels = range > 0 ? (int) Math.round(this.value * range) : 0;
                if (isX) {
                    modConfig.setRenderingPositionX(pixels);
                } else {
                    modConfig.setRenderingPositionY(pixels);
                }
                // 获取当前GUI尺寸用于更新消息
                Minecraft mc = Minecraft.getInstance();
                int guiWidth = mc.getWindow().getGuiScaledWidth();
                int guiHeight = mc.getWindow().getGuiScaledHeight();
                updateRenderingMessages(guiWidth, guiHeight);
            }
        };
    }

    private TextComponent toggleMaxStrengthMessage() {
        return new TextComponent("显示最大强度: " + (modConfig.isRenderingMax() ? "开" : "关"));
    }

    private Component positionText(int guiScaledWidth, int guiScaledHeight, boolean isX) {
        boolean hidden = modConfig.getRenderingPositionX() >= guiScaledWidth
                || modConfig.getRenderingPositionY() >= guiScaledHeight;
        if (hidden) {
            return new TextComponent("强度HUD已隐藏");
        }
        int value = isX ? modConfig.getRenderingPositionX() : modConfig.getRenderingPositionY();
        return new TextComponent((isX ? "位置X: " : "位置Y: ") + value);
    }

    private void updateRenderingMessages(int guiScaledWidth, int guiScaledHeight) {
        renderingPositionX.setMessage(positionText(guiScaledWidth, guiScaledHeight, true));
        renderingPositionY.setMessage(positionText(guiScaledWidth, guiScaledHeight, false));
    }
}
