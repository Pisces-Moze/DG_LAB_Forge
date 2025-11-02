package online.kbpf.dg_lab.client.entity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class NetworkAdapter {
    private final Map<String, String> networkMap = new HashMap<>();
    private boolean networkDetectionSuccessful = false;

    public NetworkAdapter() {
        try {
            GetAllINC();
            networkDetectionSuccessful = true;
        } catch (Exception e) {
            // 记录错误但不抛出异常，允许模组继续运行
            System.err.println("网络接口检测失败: " + e.getMessage());
            networkDetectionSuccessful = false;
        }
    }

    public Map<String, String> getNetworkMap() {
        return new HashMap<>(networkMap); // 返回副本避免外部���改
    }

    public String NICGetaddress(String NIC) {
        return networkMap.get(NIC);
    }

    public boolean isNetworkDetectionSuccessful() {
        return networkDetectionSuccessful;
    }

    public void GetAllINC() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                throw new RuntimeException("无法获取网络接口列表");
            }

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface == null || networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                try {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress inetAddress = addresses.nextElement();
                        if (inetAddress instanceof java.net.Inet4Address && inetAddress.getHostAddress() != null) {
                            String displayName = networkInterface.getDisplayName();
                            if (displayName != null && !displayName.trim().isEmpty()) {
                                networkMap.put(displayName, inetAddress.getHostAddress());
                            }
                        }
                    }
                } catch (Exception e) {
                    // 单个网络接口失败不影响其他接口
                    System.err.println("处理网络接口失败: " + networkInterface.getDisplayName() + " - " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("网络接口检测失败: " + e.getMessage(), e);
        }
    }
}
