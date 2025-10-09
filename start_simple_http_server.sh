#!/bin/bash

echo "正在启动简单HTTP后端服务..."

# 设置Java环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-24.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 进入项目目录
cd /Users/songyidemac/Desktop/222

# 创建目录
mkdir -p target/classes

# 创建简单的HTTP服务器
cat > target/classes/SimpleHttpServer.java << 'EOF'
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;

public class SimpleHttpServer {
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // 根路径
        server.createContext("/", new RootHandler());
        
        // 健康检查
        server.createContext("/health", new HealthHandler());
        
        // API测试
        server.createContext("/api/test", new ApiTestHandler());
        
        // API状态
        server.createContext("/api/status", new ApiStatusHandler());
        
        // 设置线程池
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // 启动服务器
        server.start();
        
        System.out.println("Archive Management System Backend Server started!");
        System.out.println("Server is running on: http://localhost:8080");
        System.out.println("Health check: http://localhost:8080/health");
        System.out.println("API test: http://localhost:8080/api/test");
        System.out.println("API status: http://localhost:8080/api/status");
        System.out.println("Press Ctrl+C to stop the server");
        
        // 保持服务器运行
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.stop(0);
        }));
        
        // 防止主线程退出
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Archive Management System Backend is running successfully!";
            sendResponse(exchange, 200, response);
        }
    }
    
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = String.format(
                "{\"status\":\"UP\",\"service\":\"archive-management\",\"timestamp\":\"%s\"}",
                LocalDateTime.now()
            );
            sendJsonResponse(exchange, 200, response);
        }
    }
    
    static class ApiTestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = String.format(
                "{\"message\":\"Backend API is working\",\"timestamp\":\"%s\",\"version\":\"1.0.0\"}",
                LocalDateTime.now()
            );
            sendJsonResponse(exchange, 200, response);
        }
    }
    
    static class ApiStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"backend\":\"running\",\"database\":\"h2-memory\",\"port\":8080}";
            sendJsonResponse(exchange, 200, response);
        }
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        exchange.sendResponseHeaders(statusCode, response.getBytes("UTF-8").length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes("UTF-8"));
        os.close();
    }
    
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        exchange.sendResponseHeaders(statusCode, response.getBytes("UTF-8").length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes("UTF-8"));
        os.close();
    }
}
EOF

# 编译Java文件
echo "编译HTTP服务器..."
javac -d target/classes target/classes/SimpleHttpServer.java

if [ $? -ne 0 ]; then
    echo "编译失败"
    exit 1
fi

echo "启动HTTP服务器..."
java -cp target/classes SimpleHttpServer