package com.example.modbusapplication.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Entity.DeviceMapping;
import com.example.modbusapplication.Repository.DeviceMappingRepository;
import com.example.modbusapplication.Repository.ModbusRecordRepository;

@Service
public class AdminLogicService {

    @Autowired
    ModbusRecordRepository modbusRecordRepository;

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


}
