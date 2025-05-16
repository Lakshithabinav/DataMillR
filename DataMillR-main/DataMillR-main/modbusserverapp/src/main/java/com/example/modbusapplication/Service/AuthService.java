package com.example.modbusapplication.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Entity.loginInformation;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Repository.AuthSessionRepository;
import com.example.modbusapplication.Repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final AuthSessionRepository authSessionRepository;

    private static final String SEPARATOR = "|||"; // hardcoded safe separator

    public AuthService(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    public String generateRandomNumber() {
        return String.valueOf(new Random().nextInt(10));
    }

    public String storeAuthSession(String ipAddress) {
        String randomNumber = generateRandomNumber();
        Optional<loginInformation> existingSession = authSessionRepository.findByIpAddress(ipAddress);

        loginInformation session = existingSession.orElse(new loginInformation());
        session.setIpAddress(ipAddress);
        session.setRandomNumber(randomNumber);
        session.setHitTime(LocalDateTime.now());
        session.setUserkey(0);
        authSessionRepository.save(session);

        return randomNumber;
    }

    public boolean authenticateUser(String userId, String hashedPassword, String ipAddress) {
        // System.out.println("Received Username: " + username);
        // System.out.println("Received Hashed Password (Encoded): " + hashedPassword);
        // System.out.println("Received IP Address: " + ipAddress);

        Optional<loginInformation> authSessionOptional = authSessionRepository.findByIpAddress(ipAddress);

        if (authSessionOptional.isPresent()) {
            loginInformation authSession = authSessionOptional.get();
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

}
