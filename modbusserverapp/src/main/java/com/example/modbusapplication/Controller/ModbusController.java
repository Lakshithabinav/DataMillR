package com.example.modbusapplication.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.modbusapplication.Model.DailyDataDao;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Model.RawRecordDTO;
import com.example.modbusapplication.Service.ModbusRecordService;

import java.util.Hashtable;
import java.util.List;

@RestController
@RequestMapping("/modbus")
public class ModbusController {

    @Autowired
    private ModbusRecordService modbusRecordService;

    /** ---------------- NORMAL MODBUS DATA ---------------- */
    @PostMapping("/upload-bytes")
    public ResponseEntity<String> modbusRecords(@RequestBody List<RawRecordDTO> rawRecordDTOList) {
        System.out.println("Received encoded DTO records: " + rawRecordDTOList.size());
        Hashtable<Short,DailyDataDao> dailyDataDaoHT = new Hashtable<>();

        int successCount = 0;
        for (RawRecordDTO dto : rawRecordDTOList) {
            boolean success = modbusRecordService.decodeAndStore(dto.getEncByteString(),dailyDataDaoHT);
            modbusRecordService.updateDailyData(dailyDataDaoHT); 
            
            if (success) {
                successCount++;
            } else {
                System.err.println("Failed to store decoded Modbus data for one record.");
            }
        }

        if (successCount == rawRecordDTOList.size()) {
            return ResponseEntity.ok("All records decoded and stored successfully.");
        } else if (successCount > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some records were stored successfully. " + successCount + "/" + rawRecordDTOList.size());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store any records.");
        }
    }

    @PostMapping("/upload-data")
    public ResponseEntity<String> data(@RequestBody List<ModbusEntityDao> modbusEntityDaos) {
        System.out.println("Received pre-decoded DTO records: " + modbusEntityDaos.size());

        int successCount = 0;
        for (ModbusEntityDao modbusEntityDao : modbusEntityDaos) {
            boolean success = modbusRecordService.storeData(modbusEntityDao);
            if (success) {
                successCount++;
            } else {
                System.err.println("Failed to insert ModbusEntityDao with deviceId: " + modbusEntityDao.getDeviceId());
            }
        }

        if (successCount == modbusEntityDaos.size()) {
            return ResponseEntity.ok("All records stored successfully.");
        } else if (successCount > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some records were stored successfully. " + successCount + "/" + modbusEntityDaos.size());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store any records.");
        }
    }

    /** ---------------- PACKING MACHINE DATA ---------------- */
    @PostMapping("/upload-packing-bytes")
    public ResponseEntity<String> packingRecords(@RequestBody List<RawRecordDTO> rawRecordDTOList) {
        System.out.println("Received encoded packing DTO records: " + rawRecordDTOList.size());

        int successCount = 0;
        for (RawRecordDTO dto : rawRecordDTOList) {
            boolean success = modbusRecordService.decodeAndStorePacking(dto.getEncByteString());
            if (success) {
                successCount++;
            } else {
                System.err.println("Failed to store decoded packing data for one record.");
            }
        }

        if (successCount == rawRecordDTOList.size()) {
            return ResponseEntity.ok("All packing records decoded and stored successfully.");
        } else if (successCount > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some packing records were stored successfully. " + successCount + "/" + rawRecordDTOList.size());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store any packing records.");
        }
    }

    @PostMapping("/upload-packing-data")
    public ResponseEntity<String> packingData(@RequestBody List<PackingEntityDao> modbusEntityDaos) {
        System.out.println("Received pre-decoded packing DTO records: " + modbusEntityDaos.size());

        int successCount = 0;
        for (PackingEntityDao modbusEntityDao : modbusEntityDaos) {
            boolean success = modbusRecordService.storePackingData(modbusEntityDao);
            if (success) {
                successCount++;
            } else {
                System.err.println("Failed to insert Packing ModbusEntityDao with deviceId: " + modbusEntityDao.getDeviceId());
            }
        }
        if (successCount == modbusEntityDaos.size()) {
            return ResponseEntity.ok("All packing records stored successfully.");
        } else if (successCount > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some packing records were stored successfully. " + successCount + "/" + modbusEntityDaos.size());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store any packing records.");
        }
    }
}
