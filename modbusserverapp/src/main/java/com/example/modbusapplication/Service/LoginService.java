package com.example.modbusapplication.Service;

import java.time.LocalDateTime;
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

import jakarta.transaction.Transactional;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    private final LoginSessionRepository authSessionRepository;

    private static final String SEPARATOR = "|||"; // hardcoded safe separator

    public LoginService(LoginSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    public String generateRandomNumber() {
        return String.valueOf(new Random().nextInt(10));
    }

    public String storeAuthSession(String ipAddress) {
        String randomNumber = generateRandomNumber();
        Optional<LoginInformation> existingSession = authSessionRepository.findByIpAddress(ipAddress);

        LoginInformation session = existingSession.orElse(new LoginInformation());
        session.setIpAddress(ipAddress);
        session.setRandomNumber(randomNumber);
        session.setHitTime(LocalDateTime.now());
        session.setUserKey(0);
        session.setStatus(false);
        authSessionRepository.save(session);

        return randomNumber;
    }

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

    @Scheduled(fixedRate = 60 * 60 * 1000) // every 1 hour
    @Transactional
    public void cleanOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);
        authSessionRepository.deleteOlderThan(cutoff);
        System.out.println("Old login sessions cleaned at " + LocalDateTime.now());
}

}
