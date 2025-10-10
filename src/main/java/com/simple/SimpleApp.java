package com.simple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class SimpleApp {
    
    @GetMapping("/")
    public String home() {
        return "Archive Management System Backend is running successfully!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"archive-management\",\"timestamp\":\"" + 
               java.time.LocalDateTime.now() + "\"}";
    }
    
    @GetMapping("/api/test")
    public String test() {
        return "{\"message\":\"Backend API is working\",\"timestamp\":\"" + 
               java.time.LocalDateTime.now() + "\",\"version\":\"1.0.0\"}";
    }
    
    @GetMapping("/api/status")
    public String status() {
        return "{\"backend\":\"running\",\"database\":\"h2-memory\",\"port\":8080}";
    }
    
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "h2");
        System.setProperty("server.port", "8080");
        System.setProperty("spring.datasource.url", "jdbc:h2:mem:testdb");
        System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
        System.setProperty("spring.h2.console.enabled", "true");
        
        SpringApplication.run(SimpleApp.class, args);
    }
}