package online.kbpf.dg_lab.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.Config.ModConfig;
import online.kbpf.dg_lab.client.DgLabClient;
import online.kbpf.dg_lab.client.createQR.ToolQR;
import online.kbpf.dg_lab.client.entity.NetworkAdapter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_HEIGHT;
import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_SPACING;

public class WebSocketConfigScreen extends Screen {

    private ModConfig modConfig = DgLabClient.getModConfig();
    private final NetworkAdapter networkAdapter = new NetworkAdapter();
    private final LinkedHashMap<String, String> networkMap = new LinkedHashMap<>(networkAdapter.getNetworkMap());

    private ButtonWidget autoStartButton;
    private TextFieldWidget hostField;
    private TextFieldWidget portField;
    private TextFieldWidget serverPortField;

    private Timer debounceTimer = new Timer();

    public WebSocketConfigScreen() {
        super(new TextComponent("WebSocket设置"));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ConfigScreen());
    }

    @Override
    public void removed() {
        cancelDebounce();
        super.removed();
    }

    @Override
    protected void init() {
        this.modConfig = DgLabClient.getModConfig();

        // 使用正确的屏幕尺寸
        int screenWidth = this.width;
        int screenHeight = this.height;

        int leftColumnX = (int) (screenWidth / 2.0 - (screenWidth * 0.41));
        int rightColumnX = screenWidth / 2 + 5;

        autoStartButton = ButtonWidget.builder(new TextComponent(autoStartLabel(modConfig.getAutoStartWebSocketServer())), button -> {
                    boolean next = !modConfig.getAutoStartWebSocketServer();
                    modConfig.setAutoStartWebSocketServer(next);
                    button.setMessage(new TextComponent(autoStartLabel(next)));
                })
                .dimensions(leftColumnX, 20, (int) (screenWidth * 0.4), BUTTON_HEIGHT)
                .tooltip(new TextComponent("嵌入的WebSocket服务器是否随客户端自动启动。"))
                .build();

        ButtonWidget createQrButton = ButtonWidget.builder(new TextComponent("创建二维码并打开"), button -> ToolQR.CreateQR())
                .dimensions(rightColumnX, 20, (int) (screenWidth * 0.4), BUTTON_HEIGHT)
                .tooltip(new TextComponent("为移动应用生成二维码图像。\n默认保存路径: " + System.getProperty("user.dir")))
                .build();

        int hostFieldY = 20 + BUTTON_HEIGHT + BUTTON_SPACING;
        int fieldWidth = (int) (screenWidth * 0.25);

        hostField = new TextFieldWidget(this.font, (int) (screenWidth * 0.66), hostFieldY, fieldWidth, BUTTON_HEIGHT, new TextComponent("输入地址..."));
        hostField.setValue(modConfig.getAddress());
        hostField.setResponder(this::onHostChanged);

        ButtonWidget hostInfo = ButtonWidget.builder(new TextComponent("?"), button -> { })
                .dimensions((int) (screenWidth * 0.63), hostFieldY, (int) (screenWidth * 0.03), BUTTON_HEIGHT)
                .tooltip(new TextComponent("在二维码中使用的主机名。\n通常保持检测到的本地地址。"))
                .build();

        ButtonWidget hostToggle = ButtonWidget.builder(new TextComponent("<|>"), button -> toggleNetworkAdapter())
                .dimensions((int) (screenWidth * 0.59), hostFieldY, (int) (screenWidth * 0.04), BUTTON_HEIGHT)
                .tooltip(new TextComponent("循环遍历检测到的网络适配器。"))
                .build();

        int portFieldY = 2 * (BUTTON_HEIGHT + BUTTON_SPACING) + 20;
        portField = new TextFieldWidget(this.font, (int) (screenWidth * 0.66), portFieldY, fieldWidth, BUTTON_HEIGHT, new TextComponent("输入端口..."));
        portField.setValue(String.valueOf(modConfig.getPort()));
        portField.setResponder(this::onPortChanged);
        portField.setMaxLength(5);

        ButtonWidget portInfo = ButtonWidget.builder(new TextComponent("?"), button -> { })
                .dimensions((int) (screenWidth * 0.63), portFieldY, (int) (screenWidth * 0.03), BUTTON_HEIGHT)
                .tooltip(new TextComponent("编码在二维码中供移动应用连接的端口。"))
                .build();

        int serverPortY = 3 * (BUTTON_HEIGHT + BUTTON_SPACING) + 20;
        serverPortField = new TextFieldWidget(this.font, (int) (screenWidth * 0.66), serverPortY, fieldWidth, BUTTON_HEIGHT, new TextComponent("输入端口..."));
        serverPortField.setValue(String.valueOf(modConfig.getServerPort()));
        serverPortField.setResponder(this::onServerPortChanged);
        serverPortField.setMaxLength(5);

        ButtonWidget serverPortInfo = ButtonWidget.builder(new TextComponent("?"), button -> { })
                .dimensions((int) (screenWidth * 0.63), serverPortY, (int) (screenWidth * 0.03), BUTTON_HEIGHT)
                .tooltip(new TextComponent("嵌入的WebSocket服务器使用的端口。\n更改后重启客户端。"))
                .build();

        addRenderableWidget(createQrButton);
        addRenderableWidget(autoStartButton);
        addRenderableWidget(hostField);
        addRenderableWidget(hostInfo);
        addRenderableWidget(hostToggle);
        addRenderableWidget(portField);
        addRenderableWidget(portInfo);
        addRenderableWidget(serverPortField);
        addRenderableWidget(serverPortInfo);
    }

    private static String autoStartLabel(boolean enabled) {
        return "自动启动服务器: " + (enabled ? "开" : "关");
    }

    private void onHostChanged(String value) {
        restartDebounce(() -> modConfig.setAddress(value));
    }

    private void onPortChanged(String value) {
        restartDebounce(() -> modConfig.setPort(parsePort(value)));
    }

    private void onServerPortChanged(String value) {
        restartDebounce(() -> modConfig.setServerPort(parsePort(value)));
    }

    private int parsePort(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return (parsed >= 0 && parsed <= 65_535) ? parsed : 9999;
        } catch (NumberFormatException ex) {
            return 9999;
        }
    }

    private void restartDebounce(Runnable action) {
        cancelDebounce();
        debounceTimer = new Timer();
        debounceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        }, 1_000L);
    }

    private void cancelDebounce() {
        if (debounceTimer != null) {
            debounceTimer.cancel();
            debounceTimer.purge();
        }
    }

    private void toggleNetworkAdapter() {
        boolean foundCurrent = false;
        for (Map.Entry<String, String> entry : networkMap.entrySet()) {
            if (entry.getKey().equals(modConfig.getNetwork())) {
                foundCurrent = true;
            } else if (foundCurrent) {
                applyNetwork(entry);
                return;
            }
        }

        if (!networkMap.isEmpty()) {
            applyNetwork(networkMap.entrySet().iterator().next());
        }
    }

    private void applyNetwork(Map.Entry<String, String> entry) {
        modConfig.setNetwork(entry.getKey());
        modConfig.setAddress(entry.getValue());
        hostField.setValue(entry.getValue());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);

        // 使用正确的屏幕尺寸
        int screenWidth = this.width;

        this.font.drawShadow(poseStack, new TextComponent("二维码主机"), (int) (screenWidth * 0.1), 49, 0xFFFFFF);
        this.font.drawShadow(poseStack, new TextComponent(modConfig.getNetwork()), (int) (screenWidth * 0.1), 61, 0xAAAAAA);
        this.font.drawShadow(poseStack, new TextComponent("二维码端口"), (int) (screenWidth * 0.1), 74, 0xFFFFFF);
        this.font.drawShadow(poseStack, new TextComponent("服务器端口"), (int) (screenWidth * 0.1), 99, 0xFFFFFF);
    }
}
