package com.example.modbusapplication.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.LoginInformation;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Model.DeviceNameDAO;
import com.example.modbusapplication.Model.LoginResponseDAO;
import com.example.modbusapplication.Model.UpdateUserDAO;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.LoginInformationRepository;
import com.example.modbusapplication.Repository.UserInformationRepository;

import jakarta.transaction.Transactional;

@Service
public class LoginService {

    @Autowired
    UserInformationRepository userRepository;
    @Autowired
    DeviceMappingRepository deviceMappingRepository;
    @Autowired
    LoginInformationRepository authSessionRepository;

    private static final String SEPARATOR = "124 124 124";

    // public LoginService(DeviceMappingRepository deviceMappingRepository,
    // LoginInformationRepository authSessionRepository) {
    // this.deviceMappingRepository = deviceMappingRepository;
    // this.authSessionRepository = authSessionRepository;
    // }

    public String generateRandomNumber() {
        return String.valueOf(new Random().nextInt(10));
    }

    public String storeLoginInfo(String ipAddress) {
        String randomNumber = generateRandomNumber();
        Optional<LoginInformation> existingSession = authSessionRepository.findByIpAddress(ipAddress);

        LoginInformation session = existingSession.orElse(new LoginInformation());
        session.setIpAddress(ipAddress);
        session.setRandomNumber(randomNumber);
        session.setHitTime(LocalDateTime.now());
        authSessionRepository.save(session);

        return randomNumber;
    }

    public ResponseEntity<LoginResponseDAO> loginResponse(String hashedCredential, String ipAddress) {
        // 1) find session
        Optional<LoginInformation> sessionOpt = authSessionRepository.findByIpAddress(ipAddress);
        if (sessionOpt.isEmpty())
            return null;
        LoginInformation session = sessionOpt.get();

        // 2) decode
        int rand = Integer.parseInt(session.getRandomNumber());
        String[] parts = hashedCredential.split(Pattern.quote(SEPARATOR));
        if (parts.length != 2)
            return null;
        String decodedUserId = decodeAscii(parts[0].trim(), rand);
        String decodedPassword = decodeAscii(parts[1].trim(), rand);

        // 3) authenticate
        Optional<UserInformation> userOpt = userRepository.findByUserId(decodedUserId);
        if (userOpt.isEmpty())
            return null;
        UserInformation user = userOpt.get();
        boolean ok = user.getLoginPassword().equals(decodedPassword);

        // 4) update session userKey/status
        session.setUserKey(user.getUserKey());
        session.setStatus(ok);
        authSessionRepository.save(session);

        if (!ok)
            return null;

        // 5) build response payload
        List<DeviceMapping> devices = deviceMappingRepository.findByUserKey(user.getUserKey());

        Map<String, List<Short>> deviceMap = new HashMap<>();
        for (DeviceMapping device : devices) {
            deviceMap.computeIfAbsent(device.getDeviceName(), k -> new ArrayList<>()).add(device.getDeviceId());
        }

        List<String> deviceNames = new ArrayList<>();
        List<Short> deviceIds = new ArrayList<>();

        for (DeviceMapping device : devices) {
            deviceNames.add(device.getDeviceName());
            deviceIds.add(device.getDeviceId());
        }

        List<DeviceNameDAO> deviceList = new ArrayList<>();
        deviceList.add(new DeviceNameDAO(deviceNames, deviceIds));

        LoginResponseDAO response = new LoginResponseDAO(
                user.getUserId(),
                user.getUserKey(),
                user.getCompanyName(),

                user.isNewUser(),
                deviceList);

        return ResponseEntity.ok(response);
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

    @Scheduled(fixedRate = 60 * 60 * 1000) // every 1 hour
    @Transactional
    public void cleanOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);
        authSessionRepository.deleteOlderThan(cutoff);
        System.out.println("Old login sessions cleaned at " + LocalDateTime.now());
    }

    public ResponseEntity<?> updateUserIdPassword(UpdateUserDAO updateUserDAO) {
        Optional<UserInformation> userOpt = userRepository.findByUserId(updateUserDAO.getOldUserId());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User ID not found"));
        }

        UserInformation user = userOpt.get();
        if (!user.getLoginPassword().equals(updateUserDAO.getOldPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Old password is incorrect"));
        }

        boolean updated = false;
        if (updateUserDAO.getNewUserId() != null && !updateUserDAO.getNewUserId().trim().isEmpty()) {
            user.setUserId(updateUserDAO.getNewUserId());
            updated = true;
        }
        if (updateUserDAO.getNewPassword() != null && !updateUserDAO.getNewPassword().trim().isEmpty()) {
            user.setLoginPassword(updateUserDAO.getNewPassword());
            updated = true;
        }

        if(user.isNewUser()){
           user.setNewUser(false); 
        }

        if (updated) {
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Credentials updated successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "No new credentials provided to update"));
        }
    }

}
