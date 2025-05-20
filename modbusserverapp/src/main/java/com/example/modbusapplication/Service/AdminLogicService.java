package com.example.modbusapplication.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Entity.UserInformation;
import com.example.modbusapplication.Model.CompanyDetailsDao;
// import com.example.modbusapplication.Entity.loginInformation;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.ModbusRecordRepository;
import com.example.modbusapplication.Repository.UserRepository;

import org.springframework.cache.annotation.Cacheable; 

@Service
public class AdminLogicService {

    @Autowired
    ModbusRecordRepository modbusRecordRepository;

    @Autowired
    UserRepository userRepository;

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

    public ResponseEntity<?> createNewTable(String deviceId, String deviceName, String companyName) {
        try {

            short userKey = (short) (100 + new Random().nextInt(30000));
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
            return ResponseEntity.ok("User Created: " + userId + ", Password: " + password);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
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

    @Cacheable("allCompanies")
    public List<CompanyDetailsDao> getAllCompaniesCached() {
         System.out.println("Fetching from DB...");

    List<Object[]> partialResults = userRepository.queryPartialUserInformation();

    List<CompanyDetailsDao> companyDetailsDaoList = new ArrayList<>();
    for (Object[] row : partialResults) {
        CompanyDetailsDao companyDetailsDao = new CompanyDetailsDao();
        companyDetailsDao.setUserKey((Short) row[0]);;
        companyDetailsDao.setCompanyName((String) row[1]);
        // all other fields remain null
        companyDetailsDaoList.add(companyDetailsDao);
    }

    return companyDetailsDaoList;
    }


    public ResponseEntity<?> getCompanyName(String companyName){

         LevenshteinDistance ld = new LevenshteinDistance();

        List<CompanyDetailsDao> allCompanies = getAllCompaniesCached();  // Cached data

        return (ResponseEntity<?>) allCompanies.stream()
            .filter(company -> {
                int distance = ld.apply(
                    companyName.toLowerCase(),
                    company.getCompanyName().toLowerCase()
                );
                return distance <= 3;
            })
            .collect(Collectors.toList());

    }

}
