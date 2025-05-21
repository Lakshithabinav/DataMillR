package com.example.modbusapplication.Service;

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
import com.example.modbusapplication.Model.RegDeviceDAO;
// import com.example.modbusapplication.Entity.loginInformation;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.ModbusRecordRepository;
import com.example.modbusapplication.Repository.UserInformationRepository;



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
            try {
                short shDeviceId = Short.parseShort(deviceId);
                DeviceMapping deviceMapping = new DeviceMapping(shDeviceId);
                deviceMappingRepository.save(deviceMapping);
            } catch (DataIntegrityViolationException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("The Device ID already present.Try different Device ID :(");
            }
            modbusRecordRepository.createTable(deviceId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Table created sucessfully :)");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Problem in inserting in table.");
        }
    }

    public ResponseEntity<?> registerdevice(RegDeviceDAO regDeviceDAO) {
        try {
            if (!regDeviceDAO.isNewUser()) {
                short userKey = (short) (100 + new Random().nextInt(30000));
                regDeviceDAO.setUserKey(userKey);

            }

            if (!updateDeviceMapping(regDeviceDAO)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Device ID not registered"));
            }

            if (!regDeviceDAO.isNewUser()) {
                if (!createNewUser(regDeviceDAO)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Error while creating the new user"));
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(regDeviceDAO);
        } catch (Exception e) {
           return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        
    }

    public boolean createNewUser(RegDeviceDAO regDeviceDAO) {

        String cleanedCompany = regDeviceDAO.getCompanyName().replaceAll("[^a-zA-Z0-9]", "");
        String userIdPart = cleanedCompany.length() >= 6 ? cleanedCompany.substring(0, 6) : cleanedCompany;
        userIdPart = String.format("%-6s", userIdPart).replace(' ', 'X').toUpperCase();
        int deviceIdNumber = regDeviceDAO.getDeviceId();
        String suffix = String.format("%05d", deviceIdNumber);

        String userId = userIdPart + suffix;
        String password = suffix;

        UserInformation user = new UserInformation(regDeviceDAO.getUserKey(), userId, password, regDeviceDAO.getCompanyName());
        try {
            userRepository.save(user);
        } catch (Exception e) {
            System.out.println("createNewUser :: Exception :: " + e);
            return false;
        }
        regDeviceDAO.setUserId(userId);
        regDeviceDAO.setPassword(password);
        regDeviceDAO.setUserKey( regDeviceDAO.getUserKey());
        return true;

    }

    public boolean updateDeviceMapping(RegDeviceDAO regDeviceDAO) {
        try {
            short deviceId = regDeviceDAO.getDeviceId();
            Optional<DeviceMapping> existingMapping = deviceMappingRepository.findByDeviceId(deviceId);

            if (existingMapping.isPresent()) {
                DeviceMapping mapping = existingMapping.get();
                mapping.setDeviceName(regDeviceDAO.getDeviceName());
                mapping.setUserKey(regDeviceDAO.getUserKey());
                deviceMappingRepository.save(mapping);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Exception" + e);
        }
        return false;
    }

}
