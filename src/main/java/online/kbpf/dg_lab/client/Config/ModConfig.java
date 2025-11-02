package online.kbpf.dg_lab.client.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import online.kbpf.dg_lab.client.entity.NetworkAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ModConfig {

    private static final String CONFIG_PATH = "config/dg-lab/ModConfig.json";
    private static final ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();
    private static volatile boolean configLoaded = false;

    private boolean autoStartWebSocketServer = true;
    private int renderingPositionX = 20;
    private int renderingPositionY = 20;
    private int port = 9999;
    private int serverPort = 9999;
    private String address = "error";
    private String network = "unknown";
    private boolean hasAddress;
    private boolean hasNetwork;
    private boolean renderingMax;

    public ModConfig() {
        autoDetectNetwork();
    }

    public ModConfig(boolean autoStart, int x, int y) {
        this.autoStartWebSocketServer = autoStart;
        this.renderingPositionX = x;
        this.renderingPositionY = y;
        autoDetectNetwork();
    }

    private void autoDetectNetwork() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            if (localhost != null) {
                address = localhost.getHostAddress();
                hasAddress = true;
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);
                if (networkInterface != null) {
                    network = networkInterface.getDisplayName();
                    hasNetwork = true;
                }
            }
        } catch (UnknownHostException | SocketException ignored) {
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = validatePort(port);
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = validatePort(serverPort);
    }

    public String getAddress() {
        return hasAddress ? address : "error";
    }

    public void setAddress(String address) {
        this.address = address;
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            hasAddress = true;
            hasNetwork = false;

            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            if (networkInterface != null) {
                network = networkInterface.getDisplayName();
                hasNetwork = true;
            }
        } catch (UnknownHostException | SocketException ignored) {
        }
    }

    public String getNetwork() {
        return hasNetwork ? network : "unknown";
    }

    public void setNetwork(String network) {
        this.network = network;
        hasNetwork = true;
    }

    public boolean isRenderingMax() {
        return renderingMax;
    }

    public void setRenderingMax(boolean renderingMax) {
        this.renderingMax = renderingMax;
    }

    public int getRenderingPositionX() {
        return renderingPositionX;
    }

    public void setRenderingPositionX(int renderingPositionX) {
        this.renderingPositionX = Math.max(0, renderingPositionX);
    }

    public int getRenderingPositionY() {
        return renderingPositionY;
    }

    public void setRenderingPositionY(int renderingPositionY) {
        this.renderingPositionY = Math.max(0, renderingPositionY);
    }

    public boolean getAutoStartWebSocketServer() {
        return autoStartWebSocketServer;
    }

    public void setAutoStartWebSocketServer(boolean autoStartWebSocketServer) {
        this.autoStartWebSocketServer = autoStartWebSocketServer;
    }

    public void saveFile() {
        configLock.writeLock().lock();
        try {
            if (!configLoaded) {
                return; // 配置未完全加载时不保存
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path configPath = Paths.get(CONFIG_PATH);
            Path configDir = configPath.getParent();

            // 确保目录存在
            if (configDir != null && !Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            // 备份现有配置
            if (Files.exists(configPath)) {
                Path backupPath = Paths.get(CONFIG_PATH + ".backup");
                if (Files.exists(backupPath)) {
                    Files.delete(backupPath);
                }
                Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 保存新配置
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                gson.toJson(this, writer);
            }

        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
            // 尝试恢复备份
            Path backupPath = Paths.get(CONFIG_PATH + ".backup");
            if (Files.exists(backupPath)) {
                try {
                    Files.copy(backupPath, Paths.get(CONFIG_PATH), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {}
            }
        } finally {
            configLock.writeLock().unlock();
        }
    }

    // Preserve legacy method name used by existing code.
    public void savaFile() {
        saveFile();
    }

    public static ModConfig loadJson() {
        configLock.writeLock().lock();
        try {
            loadConfigFromFile();
            return new ModConfig(); // 构造函数会处理加载逻辑
        } catch (Exception e) {
            System.err.println("配置加载失败，使用默认配置: " + e.getMessage());
            return new ModConfig(true, 20, 20);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    private static void loadConfigFromFile() throws IOException {
        Path configPath = Paths.get(CONFIG_PATH);

        if (!Files.exists(configPath)) {
            configLoaded = true;
            return;
        }

        if (!Files.isReadable(configPath)) {
            throw new IOException("无法读取配置文件: " + configPath);
        }

        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(configPath)) {
            ModConfig config = gson.fromJson(reader, ModConfig.class);
            if (config != null) {
                // 验证和更新网络配置
                validateAndUpdateNetwork(config);
            }
        } catch (JsonSyntaxException e) {
            throw new IOException("配置文件格式错误: " + e.getMessage(), e);
        }

        configLoaded = true;
    }

    private static void validateAndUpdateNetwork(ModConfig config) {
        try {
            NetworkAdapter networkAdapter = new NetworkAdapter();
            if (!config.hasAddress || (config.hasNetwork && networkAdapter.getNetworkMap().size() == 1)) {
                config.autoDetectNetwork();
            } else if (config.hasNetwork) {
                String detectedAddress = networkAdapter.NICGetaddress(config.network);
                if (detectedAddress != null && !detectedAddress.equals(config.address)) {
                    config.setAddress(detectedAddress);
                }
            }
        } catch (Exception e) {
            System.err.println("网络配置验证失败: " + e.getMessage());
            config.autoDetectNetwork(); // 回退到自动检测
        }
    }

    private int validatePort(int value) {
        return (value >= 0 && value <= 65_535) ? value : 9999;
    }
}
