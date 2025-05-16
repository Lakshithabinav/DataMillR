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

    // @Autowired
    // loginInformation loginInformation;

    public ResponseEntity<?> createNewTable(String deviceId) {
        try {
            try{
                short shDeviceId = Short.parseShort(deviceId.trim());
                DeviceMapping deviceMapping = new DeviceMapping(shDeviceId);
                deviceMappingRepository.save(deviceMapping);
            }
            catch(DataIntegrityViolationException e){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The Device ID already present.Try different Device ID :(");
            }
            modbusRecordRepository.createTable(deviceId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Table created sucessfully :)");

        } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Problem in inserting in table: ");
    }
    }
    
   public ResponseEntity<?> registerDeviceAndUser(short deviceId, String deviceName, String companyName) {
    try {
        Integer maxUserKey = userRepository.findMaxUserKey();
        short userKey = (short) ((maxUserKey == null) ? 1 : maxUserKey + 1);

        String cleanedCompany = companyName.replaceAll("[^a-zA-Z0-9]", "");
        String userIdPart = cleanedCompany.length() >= 6 ? cleanedCompany.substring(0, 6) : cleanedCompany;
        userIdPart = String.format("%-6s", userIdPart).replace(' ', 'X');
        userIdPart = userIdPart.toUpperCase();

        String suffix = String.format("%05d", deviceId);
        String userId = userIdPart + suffix;
        String password = suffix; 

        UserInformation user = new UserInformation(userKey, userId, password, companyName);
        userRepository.save(user);

        DeviceMapping device = new DeviceMapping(userKey, deviceId, deviceName);
        deviceMappingRepository.save(device);
        System.out.println("userkey"+userKey);

        return ResponseEntity.ok("User Created: " + userId + ", Password: " + password);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}

}
