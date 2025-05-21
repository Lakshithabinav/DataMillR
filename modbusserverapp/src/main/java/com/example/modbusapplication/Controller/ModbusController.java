package com.example.modbusapplication.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.RawRecordDTO;
import com.example.modbusapplication.Service.ModbusRecordService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ModbusController {

    @Autowired
    private ModbusRecordService modbusRecordService;


    @PostMapping("/upload-bytes")
    public ResponseEntity<String> modbusRecords(@RequestBody List<RawRecordDTO> rawRecordDTOList) {
        System.out.println("Received DTO records: " + rawRecordDTOList.size());

        int successCount = 0;

        for (RawRecordDTO dto : rawRecordDTOList) {
            boolean success = modbusRecordService.decodeAndStore(dto.getEncByteString());
            if (success) {
                successCount++;
            }
        }

        if (successCount == rawRecordDTOList.size()) {
            return ResponseEntity.ok("All records decoded and stored.");
        } else if (successCount > 0) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some records were stored successfully. " + successCount + "/" + rawRecordDTOList.size());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to store any records.");
        }
    }

@PostMapping("/data")
public ResponseEntity<?> getModbusData(@RequestBody ModbusDataRequestDAO requestDAO) {
    try {
        List<ModbusEntityDao> data = modbusRecordService.fetchModbusData(requestDAO);
        return ResponseEntity.ok(data);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", "Something went wrong"));
    }
}



}