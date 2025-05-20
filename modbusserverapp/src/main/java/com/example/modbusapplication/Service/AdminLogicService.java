package com.example.modbusapplication.Service;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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
import com.example.modbusapplication.Repository.UserInformationRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminLogicService {

    @Autowired
    ModbusRecordRepository modbusRecordRepository;

    @Autowired
    UserInformationRepository userRepository;

    @Autowired
    DeviceMappingRepository deviceMappingRepository;
 public ResponseEntity<?> createNewTable(String deviceId) {
        try {
            try{
                short shDeviceId = Short.parseShort(deviceId);
                DeviceMapping deviceMapping = new DeviceMapping(shDeviceId);
                deviceMappingRepository.save(deviceMapping);
            }
            catch(DataIntegrityViolationException e){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The Device ID already present.Try different Device ID :(");
            }
            modbusRecordRepository.createTable(deviceId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Table created sucessfully :)");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Problem in inserting in table.");
        }
    }

public Map<String, String> registerdevice(String deviceId, String deviceName, String companyName) {
    Map<String, String> response = new HashMap<>();
    try {
        short userKey = (short)(100 + new Random().nextInt(30000));
        String cleanedCompany = companyName.replaceAll("[^a-zA-Z0-9]", "");
        String userIdPart = cleanedCompany.length() >= 6 ? cleanedCompany.substring(0, 6) : cleanedCompany;
        userIdPart = String.format("%-6s", userIdPart).replace(' ', 'X').toUpperCase();
        int deviceIdNumber = Integer.parseInt(deviceId);
        String suffix = String.format("%05d", deviceIdNumber); 

        String userId = userIdPart + suffix;
        String password = suffix;

        short shDeviceId = Short.parseShort(deviceId);
        UserInformation user = new UserInformation(userKey, userId, password, companyName);
        userRepository.save(user);

        saveOrUpdateDeviceMapping(shDeviceId, deviceName, userKey);

        response.put("userId", userId);
        response.put("password", password);

    } catch (Exception e) {
        response.put("error", "Error: " + e.getMessage());
    }
    return response;
}

public ResponseEntity<?> saveOrUpdateDeviceMapping(short deviceId, String deviceName, short userKey) {
    Optional<DeviceMapping> existingMapping = deviceMappingRepository.findByDeviceId(deviceId);

    if (existingMapping.isPresent()) {
        DeviceMapping mapping = existingMapping.get();
        mapping.setDeviceName(deviceName);
        mapping.setUserKey(userKey);
        deviceMappingRepository.save(mapping);
        return ResponseEntity.ok("Device mapping updated successfully.");
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("Error: Device ID " + deviceId + " not found. Cannot update.");
    }
}
public ResponseEntity<?> findUserByCompanyName(String companyName) {
    Optional<UserInformation> userOpt = userRepository.findByCompanyName(companyName);
    if (userOpt.isPresent()) {
        return ResponseEntity.ok(userOpt.get());
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User with company name '" + companyName + "' not found.");
    }
}

@Transactional
public ResponseEntity<?> mapDeviceToExistUser(String deviceId, String companyName) {
    Optional<UserInformation> userOpt = userRepository.findByCompanyName(companyName);
    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Company not found.");
    }

    UserInformation user = userOpt.get();
    short shDeviceId = Short.parseShort(deviceId);
    // Check if deviceId already exists (optional but recommended)
    Optional<DeviceMapping> existingMapping = deviceMappingRepository.findByDeviceId(shDeviceId);
    if (existingMapping.isPresent()) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Device ID already mapped.");
    }
    short userkey = (short) user.getUserKey();

    // Save new mapping
    DeviceMapping mapping = new DeviceMapping();
    mapping.setDeviceId(shDeviceId);
    mapping.setUserKey(userkey); 


    deviceMappingRepository.save(mapping);

    return ResponseEntity.ok("Device ID mapped to user successfully.");
}


}





