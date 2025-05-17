package com.example.modbusapplication.Controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.modbusapplication.Service.LoginService;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService authService;

    public LoginController(LoginService authService) {
        this.authService = authService;
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initAuth(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        ipAddress = normalizeIp(ipAddress);
        System.out.println("Received IP Address: " + ipAddress);

        String randomNumber = authService.storeAuthSession(ipAddress);
        return ResponseEntity.ok(Map.of("randomNumber", randomNumber));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        String userId = requestBody.get("userId");
        String hashedPassword = requestBody.get("hashedPassword");

        String ipAddress = getClientIp(request);
        ipAddress = normalizeIp(ipAddress);

        System.out.println("Received Username: " + userId);
        System.out.println("Received Hashed Password (Encoded): " + hashedPassword);
        System.out.println("Received IP Address: " + ipAddress);


        boolean isAuthenticated = authService.authenticateUser(userId, hashedPassword, ipAddress);

        if (isAuthenticated) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Invalid username or password"));
        }
    }

   private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        return ip.split(",")[0].trim(); // In case of multiple IPs
    }

    ip = request.getHeader("X-Real-IP"); // some proxies use this
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        return ip;
    }

    return request.getRemoteAddr(); // fallback
}


    private String normalizeIp(String ip) {
        if (ip == null) return "UNKNOWN";
        if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}
