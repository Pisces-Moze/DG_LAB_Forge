package online.kbpf.dg_lab.client.webSocketServer;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import online.kbpf.dg_lab.client.entity.DGStrength;
import online.kbpf.dg_lab.client.entity.clientInfo;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import static online.kbpf.dg_lab.client.DgLabClient.waveformMap;

public class webSocketServer extends WebSocketServer {

    private boolean isRunning;
    private boolean isConnected;
    private WebSocket client;
    private clientInfo clientInfo = new clientInfo("bind", "1234-123456789-12345-12345-00", "", "targetId");
    private DGStrength dgStrength = new DGStrength();
    private Timer heartbeatTimer;

    public webSocketServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (isConnected) {
            conn.send("{\"type\":\"error\",\"message\":\"400\"}");
            return;
        }

        dgStrength = new DGStrength();
        isConnected = true;
        client = conn;
        client.send(new Gson().toJson(clientInfo, clientInfo.class));
        sendWelcomeMessage();
        startHeartbeat(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn.equals(client)) {
            stopHeartbeat();
            isConnected = false;
            client = null;
            clientInfo = new clientInfo("bind", "1234-123456789-12345-12345-00", "", "targetId");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        clientInfo incoming = new Gson().fromJson(message, clientInfo.class);
        if (incoming == null) {
            return;
        }

        if ("DGLAB".equals(incoming.getMessage())
                && "bind".equals(incoming.getType())
                && "1234-123456789-12345-12345-01".equals(incoming.getClientId())
                && incoming.getTargetId().equals(clientInfo.getClientId())) {
            clientInfo = incoming;
            clientInfo.setMessage("200");
            client.send(new Gson().toJson(clientInfo, clientInfo.class));
            return;
        }

        if (!"msg".equals(incoming.getType())) {
            return;
        }

        handleStrengthPayload(incoming.getMessage());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        isRunning = true;
    }

    public void sendStrengthToClient(int value, int mode, int channel) {
        if (!isConnected) {
            return;
        }
        clientInfo.setType("msg");
        clientInfo.setMessage("strength-" + channel + '+' + mode + '+' + value);
        client.send(new Gson().toJson(clientInfo, clientInfo.class));
    }

    public void setDelayTime(int aDelay, int bDelay) {
        dgStrength.setADelayTime(aDelay);
        dgStrength.setBDelayTime(bDelay);
    }

    public void setStrength(DGStrength strength) {
        dgStrength = strength;
    }

    public void sendStrength() {
        if (!isConnected) {
            return;
        }
        clientInfo.setType("msg");
        clientInfo.setMessage("strength-1+2+" + Math.min(dgStrength.getAStrength(), dgStrength.getAMaxStrength()));
        client.send(new Gson().toJson(clientInfo, clientInfo.class));
        clientInfo.setMessage("strength-2+2+" + Math.min(dgStrength.getBStrength(), dgStrength.getBMaxStrength()));
        client.send(new Gson().toJson(clientInfo, clientInfo.class));
    }

    public DGStrength getStrength() {
        return dgStrength;
    }

    // 保持与参考版本一致的方法名
    public void CleanFrequency(int channel) {
        cleanFrequency(channel);
    }

    public void cleanFrequency(int channel) {
        if (!isConnected) {
            return;
        }
        clientInfo.setType("msg");
        clientInfo.setMessage(channel == 1 ? "clear-1" : "clear-2");
        client.send(new Gson().toJson(clientInfo, clientInfo.class));
    }

    public void sendDgWaveform(int mode, boolean cleanPrevious, int channel) {
        if (!isConnected) {
            return;
        }
        if (cleanPrevious) {
            cleanFrequency(channel);
        }
        clientInfo.setType("msg");
        String key = switch (mode) {
            case 2 -> channel == 1 ? "ADamage" : "BDamage";
            case 3 -> channel == 1 ? "AHealing" : "BHealing";
            default -> null;
        };
        if (key != null && waveformMap.containsKey(key)) {
            clientInfo.setMessage((channel == 1 ? "pulse-A:[" : "pulse-B:[") + waveformMap.get(key).getWaveform() + "]");
            client.send(new Gson().toJson(clientInfo, clientInfo.class));
        }
    }

    public void sendDGWaveForm(String message, int channel) {
        if (!isConnected) {
            return;
        }
        cleanFrequency(channel);
        clientInfo.setType("msg");
        clientInfo.setMessage((channel == 1 ? "pulse-A:[" : "pulse-B:[") + message + "]");
        client.send(new Gson().toJson(clientInfo, clientInfo.class));
    }

    public boolean getState() {
        return isRunning;
    }

    public boolean getConnected() {
        return isConnected;
    }

    private void handleStrengthPayload(String payload) {
        if (payload == null || payload.isEmpty()) {
            return;
        }
        String[] tokens = payload.replaceAll("[^0-9]+", " ").trim().split(" ");
        if (tokens.length < 4) {
            return;
        }
        try {
            dgStrength.setAStrength(Integer.parseInt(tokens[0]));
            dgStrength.setBStrength(Integer.parseInt(tokens[1]));
            dgStrength.setAMaxStrength(Integer.parseInt(tokens[2]));
            dgStrength.setBMaxStrength(Integer.parseInt(tokens[3]));
            if (tokens.length > 4) {
                int code = Integer.parseInt(tokens[4]);
                if (code == 405) {
                    var player = Minecraft.getInstance().player;
                    if (player != null) {
                        player.displayClientMessage(
                                new TextComponent("消息长度超过1950个字符")
                                        .withStyle(style -> style.withColor(0xFF0000)),
                                false
                        );
                    }
                } else {
                    dgStrength.setBMaxStrength(code);
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void sendWelcomeMessage() {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(new TextComponent("==="), true);
            player.displayClientMessage(new TextComponent("负责任地使用此模组。作者不承担任何责任。"), true);
            player.displayClientMessage(new TextComponent("使用此模组时确保人身安全。"), true);
            player.displayClientMessage(new TextComponent("==="), true);
        }
    }

    private void startHeartbeat(WebSocket conn) {
        stopHeartbeat();
        heartbeatTimer = new Timer(true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnected) {
                    clientInfo.setType("heartbeat");
                    clientInfo.setMessage("200");
                    conn.send(new Gson().toJson(clientInfo, clientInfo.class));
                }
            }
        }, 0L, 60_000L);
    }

    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }
}
