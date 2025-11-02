package online.kbpf.dg_lab.client.createQR;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.Config.ModConfig;
import online.kbpf.dg_lab.client.DgLabClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class ToolQR {

    private ToolQR() {
    }

    public static void CreateQR() {
        try {
            // 检查依赖库
            if (!checkZXingAvailability()) {
                showError("二维码生成库不可用，请检查模组依赖");
                return;
            }

            ModConfig modConfig = DgLabClient.getModConfig();
            String ipAddress = modConfig.getAddress();

            if ("error".equals(ipAddress)) {
                showError("未检测到IP地址，请检查网络设置");
                return;
            }

            int port = modConfig.getPort();
            String url = "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#ws://"
                    + ipAddress + ':' + port + "/1234-123456789-12345-12345-01";

            // 生成并保存二维码
            generateAndSaveQRCode(url);

        } catch (Exception e) {
            showError("二维码生成过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean checkZXingAvailability() {
        try {
            Class.forName("com.google.zxing.MultiFormatWriter");
            Class.forName("com.google.zxing.common.BitMatrix");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void generateAndSaveQRCode(String url) {
        try {
            // 生成二维码图片
            BufferedImage qrImage = generateQRImage(url);

            // 安全保存文件
            File qrFile = saveQRImage(qrImage);

            // 安全打开文件
            openQRFile(qrFile);

            showSuccess("二维码已生成并保存: " + qrFile.getAbsolutePath());

        } catch (Exception e) {
            showError("二维码生成失败: " + e.getMessage());
            throw new RuntimeException("二维码生成失败", e);
        }
    }

    private static BufferedImage generateQRImage(String url) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);

        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }

    private static File saveQRImage(BufferedImage image) throws IOException {
        // 使用配置目录保存二维码
        Path configDir = Paths.get("config", "dg-lab");
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }

        File qrFile = configDir.resolve("DG_LAB_QR.png").toFile();

        // 检查文件是否可写
        if (qrFile.exists() && !qrFile.canWrite()) {
            throw new IOException("无法写入文件: " + qrFile.getAbsolutePath());
        }

        // 删除已存在的文件
        if (qrFile.exists()) {
            Files.delete(qrFile.toPath());
        }

        if (!ImageIO.write(image, "png", qrFile)) {
            throw new IOException("无法保存二维码图片");
        }

        return qrFile;
    }

    private static void openQRFile(File file) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("win")) {
                // Windows系统：使用cmd命令打开图片
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "", "\"" + file.getAbsolutePath() + "\"");
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // 检查进程是否正常启动
                if (!process.isAlive()) {
                    try {
                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            showInfo("二维码已保存，但无法自动打开，请手动查看: " + file.getAbsolutePath());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        showInfo("二维码已保存: " + file.getAbsolutePath());
                    }
                }
            } else if (osName.contains("mac")) {
                // macOS系统：使用open命令
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                // Linux/Unix系统：使用xdg-open命令
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            } else {
                // 未知系统，只保存文件
                showInfo("二维码已保存，请手动打开: " + file.getAbsolutePath());
            }

        } catch (IOException e) {
            showInfo("二维码已保存，但无法自动打开: " + e.getMessage());
            showInfo("文件位置: " + file.getAbsolutePath());
        } catch (Exception e) {
            showInfo("二维码已保存，但打开文件时发生异常: " + e.getMessage());
            showInfo("文件位置: " + file.getAbsolutePath());
        }
    }

    private static void showError(String message) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            Component component = new TextComponent(message)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF5555)));
            player.displayClientMessage(component, false);
        }
    }

    private static void showSuccess(String message) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            Component component = new TextComponent(message)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0x55FF55)));
            player.displayClientMessage(component, false);
        }
    }

    private static void showInfo(String message) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            Component component = new TextComponent(message)
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFF55)));
            player.displayClientMessage(component, false);
        }
    }
}
