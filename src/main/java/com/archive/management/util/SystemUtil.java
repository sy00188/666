package com.archive.management.util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;

/**
 * 系统信息工具类
 * 提供系统信息获取功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class SystemUtil {

    // ========== 系统基础信息 ==========

    /**
     * 获取操作系统名称
     * 
     * @return 操作系统名称
     */
    public static String getOsName() {
        return System.getProperty("os.name");
    }

    /**
     * 获取操作系统版本
     * 
     * @return 操作系统版本
     */
    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    /**
     * 获取操作系统架构
     * 
     * @return 操作系统架构
     */
    public static String getOsArch() {
        return System.getProperty("os.arch");
    }

    /**
     * 获取Java版本
     * 
     * @return Java版本
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * 获取Java供应商
     * 
     * @return Java供应商
     */
    public static String getJavaVendor() {
        return System.getProperty("java.vendor");
    }

    /**
     * 获取Java安装路径
     * 
     * @return Java安装路径
     */
    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    public static String getUserName() {
        return System.getProperty("user.name");
    }

    /**
     * 获取用户主目录
     * 
     * @return 用户主目录
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    /**
     * 获取当前工作目录
     * 
     * @return 当前工作目录
     */
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 获取临时目录
     * 
     * @return 临时目录
     */
    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 获取文件分隔符
     * 
     * @return 文件分隔符
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    /**
     * 获取路径分隔符
     * 
     * @return 路径分隔符
     */
    public static String getPathSeparator() {
        return System.getProperty("path.separator");
    }

    /**
     * 获取行分隔符
     * 
     * @return 行分隔符
     */
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    // ========== 内存信息 ==========

    /**
     * 获取内存信息
     * 
     * @return 内存信息
     */
    public static MemoryInfo getMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();
        
        MemoryInfo info = new MemoryInfo();
        info.setTotalMemory(runtime.totalMemory());
        info.setFreeMemory(runtime.freeMemory());
        info.setMaxMemory(runtime.maxMemory());
        info.setUsedMemory(runtime.totalMemory() - runtime.freeMemory());
        info.setHeapMemoryUsed(memoryBean.getHeapMemoryUsage().getUsed());
        info.setHeapMemoryMax(memoryBean.getHeapMemoryUsage().getMax());
        info.setNonHeapMemoryUsed(memoryBean.getNonHeapMemoryUsage().getUsed());
        info.setNonHeapMemoryMax(memoryBean.getNonHeapMemoryUsage().getMax());
        
        return info;
    }

    /**
     * 获取可用内存
     * 
     * @return 可用内存（字节）
     */
    public static long getAvailableMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
    }

    /**
     * 获取内存使用率
     * 
     * @return 内存使用率（百分比）
     */
    public static double getMemoryUsagePercent() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        return (double) used / max * 100;
    }

    // ========== CPU信息 ==========

    /**
     * 获取CPU核心数
     * 
     * @return CPU核心数
     */
    public static int getCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * 获取系统负载
     * 
     * @return 系统负载
     */
    public static double getSystemLoadAverage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemLoadAverage();
    }

    /**
     * 获取进程CPU使用率
     * 
     * @return CPU使用率（百分比）
     */
    public static double getProcessCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
        }
        return -1;
    }

    /**
     * 获取系统CPU使用率
     * 
     * @return CPU使用率（百分比）
     */
    public static double getSystemCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100;
        }
        return -1;
    }

    // ========== 磁盘信息 ==========

    /**
     * 获取磁盘信息
     * 
     * @return 磁盘信息列表
     */
    public static List<DiskInfo> getDiskInfo() {
        List<DiskInfo> diskInfos = new ArrayList<>();
        File[] roots = File.listRoots();
        
        for (File root : roots) {
            DiskInfo info = new DiskInfo();
            info.setPath(root.getAbsolutePath());
            info.setTotalSpace(root.getTotalSpace());
            info.setFreeSpace(root.getFreeSpace());
            info.setUsableSpace(root.getUsableSpace());
            info.setUsedSpace(root.getTotalSpace() - root.getFreeSpace());
            diskInfos.add(info);
        }
        
        return diskInfos;
    }

    /**
     * 获取指定路径的磁盘信息
     * 
     * @param path 路径
     * @return 磁盘信息
     */
    public static DiskInfo getDiskInfo(String path) {
        File file = new File(path);
        DiskInfo info = new DiskInfo();
        info.setPath(path);
        info.setTotalSpace(file.getTotalSpace());
        info.setFreeSpace(file.getFreeSpace());
        info.setUsableSpace(file.getUsableSpace());
        info.setUsedSpace(file.getTotalSpace() - file.getFreeSpace());
        return info;
    }

    // ========== 网络信息 ==========

    /**
     * 获取本机IP地址
     * 
     * @return IP地址
     */
    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /**
     * 获取本机主机名
     * 
     * @return 主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /**
     * 获取所有网络接口信息
     * 
     * @return 网络接口信息列表
     */
    public static List<NetworkInfo> getNetworkInfo() {
        List<NetworkInfo> networkInfos = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                NetworkInfo info = new NetworkInfo();
                info.setName(networkInterface.getName());
                info.setDisplayName(networkInterface.getDisplayName());
                info.setMtu(networkInterface.getMTU());
                info.setVirtual(networkInterface.isVirtual());
                info.setPointToPoint(networkInterface.isPointToPoint());
                
                List<String> addresses = new ArrayList<>();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    addresses.add(address.getHostAddress());
                }
                info.setAddresses(addresses);
                
                networkInfos.add(info);
            }
        } catch (Exception e) {
            // 忽略异常
        }
        
        return networkInfos;
    }

    // ========== 运行时信息 ==========

    /**
     * 获取JVM运行时信息
     * 
     * @return 运行时信息
     */
    public static RuntimeInfo getRuntimeInfo() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        
        RuntimeInfo info = new RuntimeInfo();
        info.setVmName(runtimeBean.getVmName());
        info.setVmVersion(runtimeBean.getVmVersion());
        info.setVmVendor(runtimeBean.getVmVendor());
        info.setStartTime(runtimeBean.getStartTime());
        info.setUptime(runtimeBean.getUptime());
        info.setClassPath(runtimeBean.getClassPath());
        info.setLibraryPath(runtimeBean.getLibraryPath());
        info.setInputArguments(runtimeBean.getInputArguments());
        
        return info;
    }

    /**
     * 获取JVM启动时间
     * 
     * @return 启动时间（毫秒）
     */
    public static long getJvmStartTime() {
        return ManagementFactory.getRuntimeMXBean().getStartTime();
    }

    /**
     * 获取JVM运行时间
     * 
     * @return 运行时间（毫秒）
     */
    public static long getJvmUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    // ========== 环境变量 ==========

    /**
     * 获取所有环境变量
     * 
     * @return 环境变量Map
     */
    public static Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    /**
     * 获取指定环境变量
     * 
     * @param name 变量名
     * @return 变量值
     */
    public static String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }

    /**
     * 获取所有系统属性
     * 
     * @return 系统属性
     */
    public static Properties getSystemProperties() {
        return System.getProperties();
    }

    /**
     * 获取指定系统属性
     * 
     * @param key 属性键
     * @return 属性值
     */
    public static String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取指定系统属性
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static String getSystemProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    // ========== 工具方法 ==========

    /**
     * 格式化字节大小
     * 
     * @param bytes 字节数
     * @return 格式化后的大小
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 格式化时间
     * 
     * @param millis 毫秒数
     * @return 格式化后的时间
     */
    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d天%d小时%d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }

    /**
     * 检查是否为Windows系统
     * 
     * @return 是否为Windows
     */
    public static boolean isWindows() {
        return getOsName().toLowerCase().contains("windows");
    }

    /**
     * 检查是否为Linux系统
     * 
     * @return 是否为Linux
     */
    public static boolean isLinux() {
        return getOsName().toLowerCase().contains("linux");
    }

    /**
     * 检查是否为Mac系统
     * 
     * @return 是否为Mac
     */
    public static boolean isMac() {
        String osName = getOsName().toLowerCase();
        return osName.contains("mac") || osName.contains("darwin");
    }

    /**
     * 获取系统信息摘要
     * 
     * @return 系统信息摘要
     */
    public static SystemSummary getSystemSummary() {
        SystemSummary summary = new SystemSummary();
        summary.setOsName(getOsName());
        summary.setOsVersion(getOsVersion());
        summary.setOsArch(getOsArch());
        summary.setJavaVersion(getJavaVersion());
        summary.setJavaVendor(getJavaVendor());
        summary.setCpuCores(getCpuCores());
        summary.setMemoryInfo(getMemoryInfo());
        summary.setDiskInfos(getDiskInfo());
        summary.setHostName(getHostName());
        summary.setIpAddress(getLocalIpAddress());
        summary.setUptime(getJvmUptime());
        
        return summary;
    }

    // ========== 内部类 ==========

    /**
     * 内存信息
     */
    public static class MemoryInfo {
        private long totalMemory;
        private long freeMemory;
        private long maxMemory;
        private long usedMemory;
        private long heapMemoryUsed;
        private long heapMemoryMax;
        private long nonHeapMemoryUsed;
        private long nonHeapMemoryMax;

        // Getters and Setters
        public long getTotalMemory() { return totalMemory; }
        public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public void setFreeMemory(long freeMemory) { this.freeMemory = freeMemory; }
        public long getMaxMemory() { return maxMemory; }
        public void setMaxMemory(long maxMemory) { this.maxMemory = maxMemory; }
        public long getUsedMemory() { return usedMemory; }
        public void setUsedMemory(long usedMemory) { this.usedMemory = usedMemory; }
        public long getHeapMemoryUsed() { return heapMemoryUsed; }
        public void setHeapMemoryUsed(long heapMemoryUsed) { this.heapMemoryUsed = heapMemoryUsed; }
        public long getHeapMemoryMax() { return heapMemoryMax; }
        public void setHeapMemoryMax(long heapMemoryMax) { this.heapMemoryMax = heapMemoryMax; }
        public long getNonHeapMemoryUsed() { return nonHeapMemoryUsed; }
        public void setNonHeapMemoryUsed(long nonHeapMemoryUsed) { this.nonHeapMemoryUsed = nonHeapMemoryUsed; }
        public long getNonHeapMemoryMax() { return nonHeapMemoryMax; }
        public void setNonHeapMemoryMax(long nonHeapMemoryMax) { this.nonHeapMemoryMax = nonHeapMemoryMax; }

        public double getUsagePercent() {
            return maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
        }

        @Override
        public String toString() {
            return "MemoryInfo{" +
                    "total=" + formatBytes(totalMemory) +
                    ", free=" + formatBytes(freeMemory) +
                    ", max=" + formatBytes(maxMemory) +
                    ", used=" + formatBytes(usedMemory) +
                    ", usage=" + String.format("%.2f%%", getUsagePercent()) +
                    '}';
        }
    }

    /**
     * 磁盘信息
     */
    public static class DiskInfo {
        private String path;
        private long totalSpace;
        private long freeSpace;
        private long usableSpace;
        private long usedSpace;

        // Getters and Setters
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public long getTotalSpace() { return totalSpace; }
        public void setTotalSpace(long totalSpace) { this.totalSpace = totalSpace; }
        public long getFreeSpace() { return freeSpace; }
        public void setFreeSpace(long freeSpace) { this.freeSpace = freeSpace; }
        public long getUsableSpace() { return usableSpace; }
        public void setUsableSpace(long usableSpace) { this.usableSpace = usableSpace; }
        public long getUsedSpace() { return usedSpace; }
        public void setUsedSpace(long usedSpace) { this.usedSpace = usedSpace; }

        public double getUsagePercent() {
            return totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
        }

        @Override
        public String toString() {
            return "DiskInfo{" +
                    "path='" + path + '\'' +
                    ", total=" + formatBytes(totalSpace) +
                    ", free=" + formatBytes(freeSpace) +
                    ", used=" + formatBytes(usedSpace) +
                    ", usage=" + String.format("%.2f%%", getUsagePercent()) +
                    '}';
        }
    }

    /**
     * 网络信息
     */
    public static class NetworkInfo {
        private String name;
        private String displayName;
        private int mtu;
        private boolean virtual;
        private boolean pointToPoint;
        private List<String> addresses;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public int getMtu() { return mtu; }
        public void setMtu(int mtu) { this.mtu = mtu; }
        public boolean isVirtual() { return virtual; }
        public void setVirtual(boolean virtual) { this.virtual = virtual; }
        public boolean isPointToPoint() { return pointToPoint; }
        public void setPointToPoint(boolean pointToPoint) { this.pointToPoint = pointToPoint; }
        public List<String> getAddresses() { return addresses; }
        public void setAddresses(List<String> addresses) { this.addresses = addresses; }

        @Override
        public String toString() {
            return "NetworkInfo{" +
                    "name='" + name + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", addresses=" + addresses +
                    ", mtu=" + mtu +
                    '}';
        }
    }

    /**
     * 运行时信息
     */
    public static class RuntimeInfo {
        private String vmName;
        private String vmVersion;
        private String vmVendor;
        private long startTime;
        private long uptime;
        private String classPath;
        private String libraryPath;
        private List<String> inputArguments;

        // Getters and Setters
        public String getVmName() { return vmName; }
        public void setVmName(String vmName) { this.vmName = vmName; }
        public String getVmVersion() { return vmVersion; }
        public void setVmVersion(String vmVersion) { this.vmVersion = vmVersion; }
        public String getVmVendor() { return vmVendor; }
        public void setVmVendor(String vmVendor) { this.vmVendor = vmVendor; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }
        public String getClassPath() { return classPath; }
        public void setClassPath(String classPath) { this.classPath = classPath; }
        public String getLibraryPath() { return libraryPath; }
        public void setLibraryPath(String libraryPath) { this.libraryPath = libraryPath; }
        public List<String> getInputArguments() { return inputArguments; }
        public void setInputArguments(List<String> inputArguments) { this.inputArguments = inputArguments; }

        @Override
        public String toString() {
            return "RuntimeInfo{" +
                    "vmName='" + vmName + '\'' +
                    ", vmVersion='" + vmVersion + '\'' +
                    ", vmVendor='" + vmVendor + '\'' +
                    ", uptime=" + formatTime(uptime) +
                    '}';
        }
    }

    /**
     * 系统信息摘要
     */
    public static class SystemSummary {
        private String osName;
        private String osVersion;
        private String osArch;
        private String javaVersion;
        private String javaVendor;
        private int cpuCores;
        private MemoryInfo memoryInfo;
        private List<DiskInfo> diskInfos;
        private String hostName;
        private String ipAddress;
        private long uptime;

        // Getters and Setters
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
        public String getOsArch() { return osArch; }
        public void setOsArch(String osArch) { this.osArch = osArch; }
        public String getJavaVersion() { return javaVersion; }
        public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
        public String getJavaVendor() { return javaVendor; }
        public void setJavaVendor(String javaVendor) { this.javaVendor = javaVendor; }
        public int getCpuCores() { return cpuCores; }
        public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
        public MemoryInfo getMemoryInfo() { return memoryInfo; }
        public void setMemoryInfo(MemoryInfo memoryInfo) { this.memoryInfo = memoryInfo; }
        public List<DiskInfo> getDiskInfos() { return diskInfos; }
        public void setDiskInfos(List<DiskInfo> diskInfos) { this.diskInfos = diskInfos; }
        public String getHostName() { return hostName; }
        public void setHostName(String hostName) { this.hostName = hostName; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }

        @Override
        public String toString() {
            return "SystemSummary{" +
                    "os='" + osName + " " + osVersion + " (" + osArch + ")" + '\'' +
                    ", java='" + javaVersion + " (" + javaVendor + ")" + '\'' +
                    ", cpu=" + cpuCores + " cores" +
                    ", memory=" + memoryInfo +
                    ", host='" + hostName + " (" + ipAddress + ")" + '\'' +
                    ", uptime=" + formatTime(uptime) +
                    '}';
        }
    }
}