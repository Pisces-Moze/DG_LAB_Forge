package online.kbpf.dg_lab.client.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import online.kbpf.dg_lab.client.Config.ModConfig;
import online.kbpf.dg_lab.client.Config.StrengthConfig;
import online.kbpf.dg_lab.client.DgLabClient;
import online.kbpf.dg_lab.client.Tool.DGWaveformTool;
import online.kbpf.dg_lab.client.Tool.FrequencyTool.FrequencyTool;
import online.kbpf.dg_lab.client.createQR.ToolQR;
import online.kbpf.dg_lab.client.entity.DGStrength;
import online.kbpf.dg_lab.client.webSocketServer.webSocketServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import static net.minecraft.commands.Commands.argument;

public final class DgLabClientCommands {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private DgLabClientCommands() {
    }

    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(buildRoot());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRoot() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("dglab")
                .executes(ctx -> {
                    sendInfo(ctx.getSource());
                    return Command.SINGLE_SUCCESS;
                });

        root.then(Commands.literal("createQR")
                .executes(ctx -> {
                    ToolQR.CreateQR();
                    sendSuccess(ctx.getSource(), "已创建二维码并尽可能打开QR.png。");
                    return Command.SINGLE_SUCCESS;
                }));

        root.then(buildStrengthBranch());
        root.then(buildWebSocketBranch());
        root.then(buildTestBranch());
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildStrengthBranch() {
        LiteralArgumentBuilder<CommandSourceStack> strength = Commands.literal("strength");

        strength.then(Commands.literal("get").executes(ctx -> {
            webSocketServer server = requireServer(ctx.getSource());
            if (server == null) {
                return 0;
            }
            sendSuccess(ctx.getSource(), GSON.toJson(server.getStrength()));
            return Command.SINGLE_SUCCESS;
        }));

        strength.then(Commands.literal("set")
                .then(argument("AStrength", IntegerArgumentType.integer(0))
                        .then(argument("BStrength", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    webSocketServer server = requireServer(ctx.getSource());
                                    if (server == null) {
                                        return 0;
                                    }
                                    DGStrength values = server.getStrength();
                                    values.setAStrength(IntegerArgumentType.getInteger(ctx, "AStrength"));
                                    values.setBStrength(IntegerArgumentType.getInteger(ctx, "BStrength"));
                                    server.setStrength(values);
                                    server.sendStrength();
                                    sendSuccess(ctx.getSource(), "已更新通道强度值。");
                                    return Command.SINGLE_SUCCESS;
                                }))));

        LiteralArgumentBuilder<CommandSourceStack> config = Commands.literal("config")
                .executes(ctx -> {
                    StrengthConfig cfg = DgLabClient.getStrengthConfig();
                    CommandSourceStack source = ctx.getSource();
                    sendSuccess(source, String.format(Locale.ROOT,
                            "伤害加成：A=%.2f B=%.2f",
                            cfg.getADamageStrength(), cfg.getBDamageStrength()));
                    sendSuccess(source, String.format(Locale.ROOT,
                            "延迟tick：A=%d B=%d",
                            cfg.getADelayTime(), cfg.getBDelayTime()));
                    sendSuccess(source, String.format(Locale.ROOT,
                            "冷却tick：A=%d B=%d",
                            cfg.getADownTime(), cfg.getBDownTime()));
                    sendSuccess(source, String.format(Locale.ROOT,
                            "冷却值：A=%d B=%d",
                            cfg.getADownValue(), cfg.getBDownValue()));
                    return Command.SINGLE_SUCCESS;
                });

        LiteralArgumentBuilder<CommandSourceStack> configSet = Commands.literal("set");

        configSet.then(Commands.literal("damageStrength")
                .then(argument("ADamageStrength", FloatArgumentType.floatArg(0.0F))
                        .then(argument("BDamageStrength", FloatArgumentType.floatArg(0.0F))
                                .executes(ctx -> {
                                    StrengthConfig cfg = DgLabClient.getStrengthConfig();
                                    cfg.setADamageStrength(FloatArgumentType.getFloat(ctx, "ADamageStrength"));
                                    cfg.setBDamageStrength(FloatArgumentType.getFloat(ctx, "BDamageStrength"));
                                    cfg.savaFile();
                                    sendSuccess(ctx.getSource(), "已保存伤害加成设置。");
                                    return Command.SINGLE_SUCCESS;
                                }))));

        configSet.then(Commands.literal("delayTime")
                .then(argument("ADelayTime", IntegerArgumentType.integer(0))
                        .then(argument("BDelayTime", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    StrengthConfig cfg = DgLabClient.getStrengthConfig();
                                    cfg.setADelayTime(IntegerArgumentType.getInteger(ctx, "ADelayTime"));
                                    cfg.setBDelayTime(IntegerArgumentType.getInteger(ctx, "BDelayTime"));
                                    cfg.savaFile();
                                    sendSuccess(ctx.getSource(), "已保存延迟设置。");
                                    return Command.SINGLE_SUCCESS;
                                }))));

        configSet.then(Commands.literal("downTime")
                .then(argument("ADownTime", IntegerArgumentType.integer(1))
                        .then(argument("BDownTime", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    StrengthConfig cfg = DgLabClient.getStrengthConfig();
                                    cfg.setADownTime(IntegerArgumentType.getInteger(ctx, "ADownTime"));
                                    cfg.setBDownTime(IntegerArgumentType.getInteger(ctx, "BDownTime"));
                                    cfg.savaFile();
                                    sendSuccess(ctx.getSource(), "已保存冷却间隔。");
                                    return Command.SINGLE_SUCCESS;
                                }))));

        configSet.then(Commands.literal("downValue")
                .then(argument("ADownValue", IntegerArgumentType.integer(0))
                        .then(argument("BDownValue", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    StrengthConfig cfg = DgLabClient.getStrengthConfig();
                                    cfg.setADownValue(IntegerArgumentType.getInteger(ctx, "ADownValue"));
                                    cfg.setBDownValue(IntegerArgumentType.getInteger(ctx, "BDownValue"));
                                    cfg.savaFile();
                                    sendSuccess(ctx.getSource(), "已保存冷却减少量。");
                                    return Command.SINGLE_SUCCESS;
                                }))));

        config.then(configSet);
        strength.then(config);
        return strength;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildWebSocketBranch() {
        LiteralArgumentBuilder<CommandSourceStack> webSocket = Commands.literal("webSocketServer");

        webSocket.then(Commands.literal("start").executes(ctx -> {
            webSocketServer server = requireServer(ctx.getSource());
            if (server == null) {
                return 0;
            }
            if (server.getState()) {
                sendSuccess(ctx.getSource(), "WebSocket服务器已在运行。");
                return Command.SINGLE_SUCCESS;
            }
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                sendSuccess(ctx.getSource(), "本地IP: " + ip);
                sendSuccess(ctx.getSource(), "确保手机和客户端在同一个局域网。");
            } catch (UnknownHostException ignored) {
                sendError(ctx.getSource(), "无法解析本地IP地址。");
            }
            try {
                server.start();
                sendSuccess(ctx.getSource(), "正在启动WebSocket服务器...");
            } catch (IllegalStateException ex) {
                sendError(ctx.getSource(), "服务器线程已启动。");
            }
            return Command.SINGLE_SUCCESS;
        }));

        webSocket.then(Commands.literal("autoStart")
                .executes(ctx -> {
                    ModConfig config = DgLabClient.getModConfig();
                    sendSuccess(ctx.getSource(), config.getAutoStartWebSocketServer()
                            ? "当前：WebSocket自动启动。"
                            : "当前：WebSocket必须手动启动。");
                    return Command.SINGLE_SUCCESS;
                })
                .then(argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> {
                            ModConfig config = DgLabClient.getModConfig();
                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                            config.setAutoStartWebSocketServer(enabled);
                            config.savaFile();
                            sendSuccess(ctx.getSource(),
                                    enabled ? "自动启动已启用。" : "自动启动已禁用。");
                            return Command.SINGLE_SUCCESS;
                        })));

        return webSocket;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildTestBranch() {
        return Commands.literal("test")
                .then(argument("text", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String input = StringArgumentType.getString(ctx, "text");
                            sendSuccess(ctx.getSource(), FrequencyTool.toFrequency(input));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("send")
                        .then(argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    webSocketServer server = requireServer(ctx.getSource());
                                    if (server == null) {
                                        return 0;
                                    }
                                    String message = StringArgumentType.getString(ctx, "text");
                                    server.sendDGWaveForm(DGWaveformTool.textToWaveform(message), 1);
                                    sendSuccess(ctx.getSource(), "波形已发送到通道A。");
                                    return Command.SINGLE_SUCCESS;
                                })));
    }

    private static void sendInfo(CommandSourceStack source) {
        sendSuccess(source, "DG-LAB客户端命令已准备。模组仍处于实验阶段。");
        sendSuccess(source, "游戏中按O打开配置界面。");
    }

    private static webSocketServer requireServer(CommandSourceStack source) {
        webSocketServer server = DgLabClient.getServer();
        if (server == null) {
            sendError(source, "WebSocket服务未初始化。请先进入一个世界。");
            return null;
        }
        return server;
    }

    private static void sendSuccess(CommandSourceStack source, String message) {
        source.sendSuccess(new TextComponent(message), false);
    }

    private static void sendError(CommandSourceStack source, String message) {
        source.sendFailure(new TextComponent(message));
    }
}
