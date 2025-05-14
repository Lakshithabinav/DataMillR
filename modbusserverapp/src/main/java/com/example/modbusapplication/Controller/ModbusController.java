package com.example.modbusapplication.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.modbusapplication.Model.RawRecordDTO;
import com.example.modbusapplication.Service.ModbusRecordService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ModbusController {

    @Autowired
    private ModbusRecordService modbusRecordService;


    @PostMapping("/upload-bytes")
    public ResponseEntity<String> uploadRawBase64DTOs(@RequestBody List<RawRecordDTO> rawRecordDTOList) {
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

}