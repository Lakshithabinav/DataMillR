package com.example.modbusapplication.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.DailyDataDTO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusRecord;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Model.RawRecordDTO;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.PackingRepository;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class ModbusRecordService {

    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    PackingRepository packingRepository;
    @Autowired
    DailyDataDTO dailyDataDTO;

    public int handleModbusData(List<RawRecordDTO> rawRecordDTOList) {
        int successCount = 0;

        try {
            for (RawRecordDTO dto : rawRecordDTOList) {
                boolean success = decodeAndStore(dto.getEncByteString(), dailyDataDTO);
                if (success) {
                    successCount++;
                } else {
                    System.err.println("Failed to store decoded Modbus data for one record.");
                }
            }

            modbusRecordRepository.insertDailyData(dailyDataDTO);
        } catch (Exception e) {
            System.out.println("Exception :: " + e);
        }
        return successCount;
    }

    public boolean decodeAndStore(String base64Data, DailyDataDTO dailyDataDTO) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data.trim());

            try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                    ObjectInputStream ois = new ObjectInputStream(bais)) {

                Object obj = ois.readObject();
                if (!(obj instanceof List<?>)) {
                    System.err.println("Decoded object is not a list.");
                    return false;
                }

                @SuppressWarnings("unchecked")
                List<ModbusRecord> records = (List<ModbusRecord>) obj;

                System.out.println("📦 Decoded ModbusRecords:");
                records.forEach(r -> System.out.println("📝 " + r));

                // Extract fields
                String batchName = null;
                int status = 0;
                int flowrate = 0;
                int setWeight = 0;
                int actualWeight = 0;
                int totalWeight = 0;
                LocalDateTime timestamp = null;
                Short deviceId = null;

                for (ModbusRecord record : records) {
                    switch (record.getName()) {
                        case "status":
                            status = Integer.parseInt(record.getRegisters());
                            break;
                        case "flowrate":
                            status = Integer.parseInt(record.getRegisters());
                            break;
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
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            break;
                        case "deviceId":
                            String rawDeviceId = record.getRegisters();
                            if (rawDeviceId != null && !rawDeviceId.trim().isEmpty()) {
                                try {
                                    deviceId = Short.parseShort(rawDeviceId.trim());
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid deviceId value: " + rawDeviceId);
                                }
                            } else {
                                System.err.println("Empty or null deviceId value");
                            }
                            break;
                    }
                }

                if (deviceId == null || batchName == null) {
                    System.err.println("Missing required fields (timestamp or batchName)");
                    return false;
                }
                //adding daily data
                dailyDataDTO.setDailyTotalweight(dailyDataDTO.getDailyTotalweight()+totalWeight);
                dailyDataDTO.setDeviceId(deviceId);

                ModbusEntityDao modbusEntityDao = new ModbusEntityDao(timestamp, status, flowrate, batchName, setWeight,
                        actualWeight, totalWeight, deviceId);

                try {
                    modbusRecordRepository.insertDataEntity(modbusEntityDao);
                    System.out.println("Record saved to database: " + modbusEntityDao);
                    return true;
                } catch (Exception e) {
                    System.err.println(
                            "Exception on insertDataEntity :: DeviceID ::" + deviceId + " :: " + e.getMessage());
                    return false;
                }

            }

        } catch (Exception e) {
            System.err.println("Error decoding/storing record: " + e.getMessage());
            return false;
        }
    }

    // public boolean storeData(ModbusEntityDao modbusEntityDao) {
    // try {
    // modbusRecordRepository.insertDataEntity(modbusEntityDao);
    // System.out.println("Record saved to database: " + modbusEntityDao);
    // return true;
    // } catch (Exception e) {
    // System.err.println("Exception storing data: " + e.getMessage());
    // return false;
    // }
    // }

    // public boolean storePackingData(PackingEntityDao modbusEntityDao) {
    // try {
    // packingRepository.insertPackingData(modbusEntityDao);
    // System.out.println("Packing record saved to database: " + modbusEntityDao);
    // return true;
    // } catch (Exception e) {
    // System.err.println("Exception storing packing data: " + e.getMessage());
    // return false;
    // }
    // }

    public boolean decodeAndStorePacking(String base64Data) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data.trim());

            try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
                    ObjectInputStream ois = new ObjectInputStream(bais)) {

                Object obj = ois.readObject();
                if (!(obj instanceof List<?>)) {
                    System.err.println("Decoded object is not a list.");
                    return false;
                }

                @SuppressWarnings("unchecked")
                List<ModbusRecord> records = (List<ModbusRecord>) obj;
                System.out.println("📦 Decoded Packing ModbusRecords:");
                records.forEach(r -> System.out.println("📝 " + r));

                // Extract fields
                LocalDateTime timestamp = null;
                String batchId = null;
                int bagWeightSet = 0;
                int actualWeight = 0;
                String scaleUsed = null;
                int bagCount = 0;
                int accumulatedWeight = 0;
                String machineStatus = null;
                Short deviceId = null;

                for (ModbusRecord record : records) {
                    switch (record.getName()) {
                        case "timestamp":
                            timestamp = LocalDateTime.parse(
                                    record.getRegisters().substring(0, 19),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            break;
                        case "batchId":
                            batchId = record.getRegisters();
                            break;
                        case "bagWeightSet":
                            bagWeightSet = Integer.parseInt(record.getRegisters());
                            break;
                        case "actualWeight":
                            actualWeight = Integer.parseInt(record.getRegisters());
                            break;
                        case "scaleUsed":
                            scaleUsed = record.getRegisters();
                            break;
                        case "bagCount":
                            bagCount = Integer.parseInt(record.getRegisters());
                            break;
                        case "accumulatedWeight":
                            accumulatedWeight = Integer.parseInt(record.getRegisters());
                            break;
                        case "machineStatus":
                            machineStatus = record.getRegisters();
                            break;
                        case "deviceId":
                            String rawDeviceId = record.getRegisters();
                            if (rawDeviceId != null && !rawDeviceId.trim().isEmpty()) {
                                try {
                                    deviceId = Short.parseShort(rawDeviceId.trim());
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid deviceId value: " + rawDeviceId);
                                }
                            } else {
                                System.err.println("Empty or null deviceId value");
                            }
                            break;
                    }
                }

                if (deviceId == null || batchId == null) {
                    System.err.println("Missing required fields (deviceId or batchId)");
                    return false;
                }

                PackingEntityDao modbusEntityDao = new PackingEntityDao(
                        timestamp, batchId, bagWeightSet, actualWeight, scaleUsed,
                        bagCount, accumulatedWeight, machineStatus, deviceId);

                packingRepository.insertPackingData(modbusEntityDao);
                System.out.println("Packing record saved: " + modbusEntityDao);
                return true;

            }

        } catch (Exception e) {
            System.err.println("Error decoding/storing packing record: " + e.getMessage());
            return false;
        }
    }

}
