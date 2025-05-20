package com.example.modbusapplication.Controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.modbusapplication.Service.LoginService;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

<<<<<<< HEAD
    private final LoginService authService;

    public LoginController(LoginService authService) {
        this.authService = authService;
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initAuth(HttpServletRequest request) {
=======
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> sendRandomNumber(HttpServletRequest request) {
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
        String ipAddress = getClientIp(request);
        ipAddress = normalizeIp(ipAddress);
        System.out.println("Received IP Address: " + ipAddress);

<<<<<<< HEAD
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
=======
        String randomNumber = loginService.storeLoginInfo(ipAddress);
        return ResponseEntity.ok(Map.of("randomNumber", randomNumber));
    }

@PostMapping("/login")
public ResponseEntity<?> loginUser(
        @RequestBody Map<String, String> requestBody,
        HttpServletRequest request) {

    String hashedCredential = requestBody.get("hashedCredential");
    String ip = normalizeIp(getClientIp(request));

    Map<String,Object> loginPayload = loginService.loginResponse(hashedCredential, ip);
    if (loginPayload != null) {
        return ResponseEntity.ok(loginPayload);
    } else {
        return ResponseEntity
            .status(401)
            .body(Map.of("success", false, "error", "Invalid username or password"));
    }
}


>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0

   private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        return ip.split(",")[0].trim(); // In case of multiple IPs
    }

    ip = request.getHeader("X-Real-IP"); // some proxies use this
    if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        return ip;
    }

<<<<<<< HEAD
    return request.getRemoteAddr(); // fallback
=======
    return request.getRemoteAddr();
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
}


    private String normalizeIp(String ip) {
        if (ip == null) return "UNKNOWN";
        if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
<<<<<<< HEAD
=======

@PostMapping("/update-credentials")
public ResponseEntity<?> updateCredentials(@RequestBody Map<String, String> requestBody) {
    String oldUserId = requestBody.get("oldUserId");
    String oldPassword = requestBody.get("oldPassword");
    String newUserId = requestBody.get("newUserId");      // can be null
    String newPassword = requestBody.get("newPassword");  // can be null

    if (oldUserId == null || oldPassword == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "Old userId and password are required"));
    }

    return loginService.updateUserIdPassword(oldUserId, oldPassword, newUserId, newPassword);
}


>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
}
