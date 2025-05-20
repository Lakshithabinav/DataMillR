package com.example.modbusapplication.Service;

import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Entity.LoginInformation;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Repository.LoginSessionRepository;
import com.example.modbusapplication.Repository.UserRepository;
=======
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.LoginInformation;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.LoginInformationRepository;
import com.example.modbusapplication.Repository.UserInformationRepository;
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0

import jakarta.transaction.Transactional;

@Service
public class LoginService {

    @Autowired
<<<<<<< HEAD
    private UserRepository userRepository;

    private final LoginSessionRepository authSessionRepository;

    private static final String SEPARATOR = "|||"; // hardcoded safe separator

    public LoginService(LoginSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }
=======
    UserInformationRepository userRepository;
    @Autowired
    DeviceMappingRepository deviceMappingRepository;
    @Autowired
    LoginInformationRepository authSessionRepository;

    private static final String SEPARATOR = "124 124 124";

// public LoginService(DeviceMappingRepository deviceMappingRepository,
//                     LoginInformationRepository authSessionRepository) {
//     this.deviceMappingRepository = deviceMappingRepository;
//     this.authSessionRepository = authSessionRepository;
// }
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0

    public String generateRandomNumber() {
        return String.valueOf(new Random().nextInt(10));
    }

<<<<<<< HEAD
    public String storeAuthSession(String ipAddress) {
=======
    public String storeLoginInfo(String ipAddress) {
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
        String randomNumber = generateRandomNumber();
        Optional<LoginInformation> existingSession = authSessionRepository.findByIpAddress(ipAddress);

        LoginInformation session = existingSession.orElse(new LoginInformation());
        session.setIpAddress(ipAddress);
        session.setRandomNumber(randomNumber);
        session.setHitTime(LocalDateTime.now());
<<<<<<< HEAD
        session.setUserKey(0);
        session.setStatus(false);
=======
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
        authSessionRepository.save(session);

        return randomNumber;
    }

<<<<<<< HEAD
    public boolean authenticateUser(String userId, String hashedPassword, String ipAddress) {
        // System.out.println("Received Username: " + username);
        // System.out.println("Received Hashed Password (Encoded): " + hashedPassword);
        // System.out.println("Received IP Address: " + ipAddress);

        Optional<LoginInformation> authSessionOptional = authSessionRepository.findByIpAddress(ipAddress);

        if (authSessionOptional.isPresent()) {
            LoginInformation authSession = authSessionOptional.get();
            int randomNumber = Integer.parseInt(authSession.getRandomNumber());
            System.out.println("Random Number: " + randomNumber);

            // Split encoded string using hardcoded separator
            String[] parts = hashedPassword.split(Pattern.quote(SEPARATOR));
            if (parts.length != 2) {
                System.out.println("Invalid encoded format. Separator not found.");
                return false;
            }

            String encodedPasswordOnly = parts[1].trim();
            String decodedPassword = decodePassword(encodedPasswordOnly, randomNumber);

            Optional<UserInformation> userOptional = userRepository.findByUserId(userId);
            if (userOptional.isPresent()) {
                UserInformation user = userOptional.get();
                return user.getPassword().equals(decodedPassword);
            }
        }

        return false;
    }

    public String decodePassword(String encodedPassword, int randomNumber) {
        String[] encodedParts = encodedPassword.split(" ");
        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < encodedParts.length; i++) {
            try {
                int asciiValue = Integer.parseInt(encodedParts[i]);
                if ((i + 1) % 2 != 0) {
                    asciiValue -= randomNumber;
                } else {
                    asciiValue += randomNumber;
                }
                decoded.append((char) asciiValue);
            } catch (NumberFormatException e) {
                System.out.println("Invalid ASCII value at index " + i + ": " + encodedParts[i]);
            }
        }

        System.out.println("Final Decoded Password: " + decoded.toString());
        return decoded.toString();
    }

=======
 public Map<String,Object> loginResponse(String hashedCredential, String ipAddress) {
        // 1) find session
        Optional<LoginInformation> sessionOpt = authSessionRepository.findByIpAddress(ipAddress);
        if (sessionOpt.isEmpty()) return null;
        LoginInformation session = sessionOpt.get();

        // 2) decode
        int rand = Integer.parseInt(session.getRandomNumber());
        String[] parts = hashedCredential.split(Pattern.quote(SEPARATOR));
        if (parts.length != 2) return null;
        String decodedUserId = decodeAscii(parts[0].trim(), rand);
        String decodedPassword = decodeAscii(parts[1].trim(), rand);

        // 3) authenticate
        Optional<UserInformation> userOpt = userRepository.findByUserId(decodedUserId);
        if (userOpt.isEmpty()) return null;
        UserInformation user = userOpt.get();
        boolean ok = user.getPassword().equals(decodedPassword);

        // 4) update session userKey/status
        session.setUserKey(user.getUserKey());
        session.setStatus(ok);
        authSessionRepository.save(session);

        if (!ok) return null;

        // 5) build response payload
    List<DeviceMapping> devices = deviceMappingRepository.findByUserKey((short) user.getUserKey());

    List<Map<String, Object>> deviceList = devices.stream().map(d -> {
    Map<String, Object> map = new HashMap<>();
    map.put("deviceId", d.getDeviceId());
    map.put("deviceName", d.getDeviceName());
    return map;
}).collect(Collectors.toList());


        Map<String,Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("companyName", user.getCompanyName());
        resp.put("devices", deviceList);
        return resp;
    }

public void updateUserKeyAndStatus(String ipAddress, int userKey, boolean status) {
    Optional<LoginInformation> sessionOpt = authSessionRepository.findByIpAddress(ipAddress);
    if (sessionOpt.isPresent()) {
        LoginInformation session = sessionOpt.get();
        session.setUserKey(userKey);
        session.setStatus(status);
        authSessionRepository.save(session);
    }
}


  public String decodeAscii(String encodedText, int randomNumber) {
    String[] encodedParts = encodedText.split(" ");
    StringBuilder decoded = new StringBuilder();

    for (int i = 0; i < encodedParts.length; i++) {
        try {
            int asciiValue = Integer.parseInt(encodedParts[i]);
            if (i % 2 == 0) {
                asciiValue -= randomNumber;
            } else {
                asciiValue += randomNumber;
            }
            decoded.append((char) asciiValue);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ASCII value at index " + i + ": " + encodedParts[i]);
        }
    }

    return decoded.toString();
}

 
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
    @Scheduled(fixedRate = 60 * 60 * 1000) // every 1 hour
    @Transactional
    public void cleanOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);
        authSessionRepository.deleteOlderThan(cutoff);
        System.out.println("Old login sessions cleaned at " + LocalDateTime.now());
}

<<<<<<< HEAD
=======
public ResponseEntity<?> updateUserIdPassword(String oldUserId, String oldPassword, String newUserId, String newPassword) {
    Optional<UserInformation> userOpt = userRepository.findByUserId(oldUserId);

    if (userOpt.isEmpty()) {
        return ResponseEntity.status(404).body(Map.of("error", "User ID not found"));
    }

    UserInformation user = userOpt.get();
    if (!user.getPassword().equals(oldPassword)) {
        return ResponseEntity.status(401).body(Map.of("error", "Old password is incorrect"));
    }

    boolean updated = false;
    if (newUserId != null && !newUserId.trim().isEmpty()) {
        user.setUserId(newUserId);
        updated = true;
    }
    if (newPassword != null && !newPassword.trim().isEmpty()) {
        user.setPassword(newPassword);
        updated = true;
    }

    if (updated) {
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Credentials updated successfully"));
    } else {
        return ResponseEntity.badRequest().body(Map.of("error", "No new credentials provided to update"));
    }
}

>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
}
