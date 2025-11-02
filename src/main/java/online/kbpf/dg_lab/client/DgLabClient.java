package online.kbpf.dg_lab.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.ClientRegistry;
import online.kbpf.dg_lab.client.Config.ModConfig;
import online.kbpf.dg_lab.client.Config.StrengthConfig;
import online.kbpf.dg_lab.client.Config.WaveformConfig;
import online.kbpf.dg_lab.client.Tool.DGWaveformTool;
import online.kbpf.dg_lab.client.entity.DGStrength;
import online.kbpf.dg_lab.client.entity.Waveform.Waveform;
import online.kbpf.dg_lab.client.screen.ConfigScreen;
import online.kbpf.dg_lab.client.webSocketServer.webSocketServer;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Client-side controller keeping shared state across Forge event callbacks.
 */
public final class DgLabClient {

    private static final KeyMapping CONFIG_KEY = new KeyMapping(
            "key.dg_lab.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.dg_lab"
    );

    public static final ModConfig modConfig = ModConfig.loadJson();
    public static Map<String, Waveform> waveformMap = WaveformConfig.LoadWaveform();
    public static StrengthConfig strengthConfig = new StrengthConfig();
    public static webSocketServer webSocketServer;

    private static Screen configScreen = new ConfigScreen();

    private static int tickCounter;
    private static int lastRunTickA;
    private static int lastRunTickB;
    private static boolean hasDetectedADelay;
    private static boolean hasDetectedBDelay;
    private static boolean clearA;
    private static boolean clearB;
    private static boolean hasDetectedADelayZeroAndStrength;
    private static boolean hasDetectedBDelayZeroAndStrength;
    private static float lastKnownHealth = Float.NaN;
    private static boolean healthInitialized = false; // 新增：健康初始化状态

    private DgLabClient() {
    }

    public static void initialize() {
        configScreen = new ConfigScreen();

        waveformMap = WaveformConfig.LoadWaveform();
        strengthConfig = StrengthConfig.loadJson();
        DGWaveformTool.updateDuration();

        // 安全初始化WebSocket服务器
        initializeWebSocketServer();

        resetTickState();
        ClientRegistry.registerKeyBinding(CONFIG_KEY);
    }

    private static void initializeWebSocketServer() {
        try {
            // 检查端口是否有效
            int port = modConfig.getServerPort();
            if (port < 1024 || port > 65535) {
                showError("无效的WebSocket端口: " + port + ". 使用默认端口9999");
                port = 9999;
                modConfig.setServerPort(port);
            }

            // 检查端口是否可用
            if (!isPortAvailable(port)) {
                showError("端口 " + port + " 已被占用，WebSocket服务器启动失败");
                return;
            }

            // 安全创建和启动服务器
            if (webSocketServer == null) {
                webSocketServer = new webSocketServer(new InetSocketAddress(port));
            }

            if (modConfig.getAutoStartWebSocketServer()) {
                webSocketServer.start();
                showSuccess("WebSocket服务器已启动在端口: " + port);
            }
        } catch (Exception e) {
            showError("WebSocket服务器启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isPortAvailable(int port) {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void showError(String message) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(
                new TextComponent(message)
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0xFF5555))), false);
        }
    }

    private static void showSuccess(String message) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(
                new TextComponent(message)
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x55FF55))), false);
        }
    }

    public static void handleClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        processHealthTick(minecraft);
        processWaveformTick(minecraft);

        while (CONFIG_KEY.consumeClick()) {
            minecraft.setScreen(configScreen);
        }
    }
    public static void renderHud(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        int hudX = modConfig.getRenderingPositionX();
        int hudY = modConfig.getRenderingPositionY();
        if (hudX >= minecraft.getWindow().getGuiScaledWidth()
                || hudY >= minecraft.getWindow().getGuiScaledHeight()) {
            return;
        }

        if (webSocketServer != null && webSocketServer.getConnected()) {
            int aStrength = webSocketServer.getStrength().getAStrength();
            int bStrength = webSocketServer.getStrength().getBStrength();
            int aMax = webSocketServer.getStrength().getAMaxStrength();
            int bMax = webSocketServer.getStrength().getBMaxStrength();

            Component lineA = modConfig.isRenderingMax()
                    ? new TextComponent(String.format("A: %d / %d", aStrength, aMax))
                    : new TextComponent(String.format("A: %d", aStrength));
            Component lineB = modConfig.isRenderingMax()
                    ? new TextComponent(String.format("B: %d / %d", bStrength, bMax))
                    : new TextComponent(String.format("B: %d", bStrength));

            minecraft.font.drawShadow(poseStack, lineA, hudX, hudY, 0xFFFFFF);
            minecraft.font.drawShadow(poseStack, lineB, hudX, hudY + 9, 0xFFFFFF);
        } else {
            minecraft.font.drawShadow(poseStack, new TextComponent("无连接"), hudX, hudY, 0xFF0000);
        }
    }

    public static webSocketServer getServer() {
        return webSocketServer;
    }

    public static StrengthConfig getStrengthConfig() {
        return strengthConfig;
    }

    public static ModConfig getModConfig() {
        return modConfig;
    }

    private static void processHealthTick(Minecraft minecraft) {
        if (webSocketServer == null || !webSocketServer.getConnected()) {
            lastKnownHealth = Float.NaN;
            healthInitialized = false;
            return;
        }

        var player = minecraft.player;
        if (player == null) {
            return;
        }

        float health = player.getHealth();

        // 健���初始化检查 - 参考ClientPlayerEntityAccessor的功能
        if (!healthInitialized) {
            lastKnownHealth = health;
            healthInitialized = true;
            return;
        }

        if (Float.isNaN(lastKnownHealth)) {
            lastKnownHealth = health;
            return;
        }

        float damage = lastKnownHealth - health;
        if (damage > 0.0F) {
            webSocketServer.setDelayTime(strengthConfig.getADelayTime(), strengthConfig.getBDelayTime());
            if (strengthConfig.getADamageStrength() > 0) {
                int value = Math.max(1, (int) (damage * strengthConfig.getADamageStrength()));
                webSocketServer.sendStrengthToClient(value, 1, 1);
            }
            if (strengthConfig.getBDamageStrength() > 0) {
                int value = Math.max(1, (int) (damage * strengthConfig.getBDamageStrength()));
                webSocketServer.sendStrengthToClient(value, 1, 2);
            }
        }

        if (health <= 0.0F) {
            webSocketServer.setDelayTime(strengthConfig.getADeathDelay(), strengthConfig.getBDeathDelay());
            DGStrength strength = webSocketServer.getStrength();
            webSocketServer.sendStrengthToClient(
                    Math.min(strength.getAStrength() + strengthConfig.getADeathStrength(), strength.getAMaxStrength()),
                    2, 1);
            webSocketServer.sendStrengthToClient(
                    Math.min(strength.getBStrength() + strengthConfig.getBDeathStrength(), strength.getBMaxStrength()),
                    2, 2);
        }

        lastKnownHealth = health;
    }

    private static void processWaveformTick(Minecraft minecraft) {
        if (webSocketServer == null || !webSocketServer.getConnected()) {
            resetTickState();
            return;
        }

        DGStrength strength = webSocketServer.getStrength();
        int aDelayTime = Math.max(0, strength.getADelayTime() - 1);
        int bDelayTime = Math.max(0, strength.getBDelayTime() - 1);
        webSocketServer.setDelayTime(aDelayTime, bDelayTime);

        int aStrength = strength.getAStrength();
        int bStrength = strength.getBStrength();

        int aMin = 0;
        int bMin = 0;
        if (minecraft.player != null) {
            float maxHealth = minecraft.player.getMaxHealth();
            float missing = maxHealth - minecraft.player.getHealth();
            float ratio = maxHealth <= 0 ? 0 : missing / maxHealth;
            aMin = (int) (strengthConfig.getAMin() * ratio);
            bMin = (int) (strengthConfig.getBMin() * ratio);
        }

        if (strengthConfig.getADownTime() > 0
                && tickCounter % strengthConfig.getADownTime() == 0
                && aDelayTime <= 0 && aStrength > aMin) {
            if (strength.getAStrength() - strengthConfig.getADownValue() < aMin) {
                webSocketServer.sendStrengthToClient(aMin, 2, 1);
            } else {
                webSocketServer.sendStrengthToClient(strengthConfig.getADownValue(), 0, 1);
            }
        }

        if (strengthConfig.getBDownTime() > 0
                && tickCounter % strengthConfig.getBDownTime() == 0
                && bDelayTime <= 0 && bStrength > bMin) {
            if (strength.getBStrength() - strengthConfig.getBDownValue() < bMin) {
                webSocketServer.sendStrengthToClient(bMin, 2, 2);
            } else {
                webSocketServer.sendStrengthToClient(strengthConfig.getBDownValue(), 0, 2);
            }
        }

        handleWaveformChannel(1, aDelayTime, aStrength);
        handleWaveformChannel(2, bDelayTime, bStrength);

        tickCounter++;
        if (tickCounter >= 2_147_483_625) {
            tickCounter = 0;
        }
    }

    private static void handleWaveformChannel(int channel, int delayTime, int strengthValue) {
        String damageKey = channel == 1 ? "ADamage" : "BDamage";
        String healingKey = channel == 1 ? "AHealing" : "BHealing";

        Waveform damageWaveform = waveformMap.get(damageKey);
        Waveform healingWaveform = waveformMap.get(healingKey);

        boolean delayFlag = channel == 1 ? hasDetectedADelay : hasDetectedBDelay;
        boolean clearedFlag = channel == 1 ? clearA : clearB;
        boolean delayZeroFlag = channel == 1 ? hasDetectedADelayZeroAndStrength : hasDetectedBDelayZeroAndStrength;
        int lastRunTick = channel == 1 ? lastRunTickA : lastRunTickB;

        if (delayTime > 0) {
            if (channel == 1) {
                hasDetectedADelayZeroAndStrength = false;
                clearA = false;
            } else {
                hasDetectedBDelayZeroAndStrength = false;
                clearB = false;
            }
            if (!delayFlag) {
                if (damageWaveform != null) {
                    webSocketServer.sendDgWaveform(2, true, channel);
                }
                setDelayFlag(channel, true);
            } else if (damageWaveform != null && tickCounter - lastRunTick >= damageWaveform.getDuration() * 2) {
                webSocketServer.sendDgWaveform(2, false, channel);
                setLastRunTick(channel, tickCounter);
            }
        } else {
            setDelayFlag(channel, false);
            if (strengthValue > 0) {
                if (!delayZeroFlag) {
                    if (healingWaveform != null) {
                        webSocketServer.sendDgWaveform(3, true, channel);
                    }
                    setDelayZeroFlag(channel, true);
                } else if (healingWaveform != null
                        && tickCounter - lastRunTick >= healingWaveform.getDuration() * 2) {
                    webSocketServer.sendDgWaveform(3, false, channel);
                    setLastRunTick(channel, tickCounter);
                }
            } else if (!clearedFlag) {
                webSocketServer.cleanFrequency(channel);
                setClearFlag(channel, true);
            }
        }
    }

    private static void setDelayFlag(int channel, boolean value) {
        if (channel == 1) {
            hasDetectedADelay = value;
        } else {
            hasDetectedBDelay = value;
        }
    }

    private static void setDelayZeroFlag(int channel, boolean value) {
        if (channel == 1) {
            hasDetectedADelayZeroAndStrength = value;
        } else {
            hasDetectedBDelayZeroAndStrength = value;
        }
    }

    private static void setClearFlag(int channel, boolean value) {
        if (channel == 1) {
            clearA = value;
        } else {
            clearB = value;
        }
    }

    private static void setLastRunTick(int channel, int value) {
        if (channel == 1) {
            lastRunTickA = value;
        } else {
            lastRunTickB = value;
        }
    }

    private static void resetTickState() {
        tickCounter = 0;
        lastRunTickA = 0;
        lastRunTickB = 0;
        hasDetectedADelay = false;
        hasDetectedBDelay = false;
        clearA = false;
        clearB = false;
        hasDetectedADelayZeroAndStrength = false;
        hasDetectedBDelayZeroAndStrength = false;
    }
}



