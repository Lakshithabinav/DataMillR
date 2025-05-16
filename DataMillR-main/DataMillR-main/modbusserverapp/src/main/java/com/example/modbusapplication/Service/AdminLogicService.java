package com.example.modbusapplication.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.UserInformation;
// import com.example.modbusapplication.Entity.loginInformation;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.ModbusRecordRepository;
import com.example.modbusapplication.Repository.UserRepository;

@Service
public class AdminLogicService {

    @Autowired
    ModbusRecordRepository modbusRecordRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DeviceMappingRepository deviceMappingRepository;


public ResponseEntity<?> createNewTable(short deviceId, String deviceName, String companyName) {
    try {
          
        Integer maxUserKey = userRepository.findMaxUserKey();
        short userKey = (short) ((maxUserKey == null) ? 1 : maxUserKey + 1);
        String cleanedCompany = companyName.replaceAll("[^a-zA-Z0-9]", "");
        String userIdPart = cleanedCompany.length() >= 6 ? cleanedCompany.substring(0, 6) : cleanedCompany;
        userIdPart = String.format("%-6s", userIdPart).replace(' ', 'X').toUpperCase();
        String suffix = String.format("%05d", deviceId);
        String userId = userIdPart + suffix;
        String password = suffix;

        try {
            DeviceMapping device = new DeviceMapping(userKey, deviceId, deviceName);
            deviceMappingRepository.save(device);
            UserInformation user = new UserInformation(userKey, userId, password, companyName);
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("The Device ID is already present. Try a different Device ID.");
        }

        modbusRecordRepository.createTable(deviceId);

        return ResponseEntity.ok("User Created: " + userId + ", Password: " + password);

    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}

}





