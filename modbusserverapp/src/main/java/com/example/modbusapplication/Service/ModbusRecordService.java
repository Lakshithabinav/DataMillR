package com.example.modbusapplication.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusRecord;
import com.example.modbusapplication.Repository.ModbusRecordRepository;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class ModbusRecordService {

   @Autowired
   ModbusRecordRepository modbusRecordRepository;

    public boolean decodeAndStore(String base64Data) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data.trim());

            try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {

                Object obj = ois.readObject();
                if (!(obj instanceof List<?>)) {
                    System.err.println("mDecoded object is not a list");
                    return false;
                }

                @SuppressWarnings("unchecked")
                List<ModbusRecord> records = (List<ModbusRecord>) obj;

                System.out.println("📦 Decoded ModbusRecords:");
                records.forEach(r -> System.out.println("📝 " + r));

                // Extract fields
                String batchName = null;
                int setWeight = 0;
                int actualWeight = 0;
                int totalWeight = 0;
                LocalDateTime timestamp = null;
                Short deviceId = null;

                for (ModbusRecord record : records) {
                    switch (record.getName()) {
                        case "batchName":
                            batchName = record.getRegisters();
                            break;
                        case "setWeight":
                            setWeight = Integer.parseInt(record.getRegisters());
                            break;
                        case "actualWeight":
                            actualWeight = Integer.parseInt(record.getRegisters());
                            break;
                        case "totalWeight":
                            totalWeight = Integer.parseInt(record.getRegisters());
                            break;
                        case "datetime":
                            timestamp = LocalDateTime.parse(
                                record.getRegisters().substring(0, 19),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            );
                            break;
                        case "deviceId":
                            deviceId = Short.parseShort(record.getRegisters());
                            break;
                    }
                }

                if (deviceId == null || batchName == null) {
                    System.err.println("Missing required fields (timestamp or batchName)");
                    return false;
                }

                ModbusEntityDao modbusEntityDao = new ModbusEntityDao(timestamp, batchName, setWeight, actualWeight, totalWeight, deviceId);
                
                modbusRecordRepository.insertDataEntity(modbusEntityDao);
                

                System.out.println("Record saved to database: " + modbusEntityDao);
                return true;
            }

        } catch (Exception e) {
            System.err.println("Error decoding/storing record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
