package com.example.modbusapplication.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.DailyDataDao;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusRecord;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Repository.ModbusRecordRepository;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

@Service
public class ModbusRecordService {

    @Autowired
    ModbusRecordRepository modbusRecordRepository;
    int sumOfTotalWeight = 0;

    public boolean decodeAndStore(String base64Data, Hashtable<Short, DailyDataDao> dailyDataDaoHT) {
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

                ModbusEntityDao modbusEntityDao = new ModbusEntityDao(timestamp, status, flowrate, batchName, setWeight,
                        actualWeight, totalWeight, deviceId);

                try {
                    modbusRecordRepository.insertDataEntity(modbusEntityDao);
                    System.out.println("Record saved to database: " + modbusEntityDao);
                    short deviceID = modbusEntityDao.getDeviceId();
                    if (dailyDataDaoHT.containsKey(deviceId)) {
                        DailyDataDao dailyDataDao = dailyDataDaoHT.get(deviceID);
                        dailyDataDao.setSumOfTotalWeight(dailyDataDao.getSumOfTotalWeight()
                                + modbusEntityDao.getTotalWeight());
                        dailyDataDaoHT.put(deviceID, dailyDataDao);
                    } else {
                        DailyDataDao dailyDataDao = new DailyDataDao();
                        dailyDataDao.setSumOfTotalWeight(modbusEntityDao.getTotalWeight());
                        dailyDataDaoHT.put(deviceId, dailyDataDao);
                    }
                    modbusEntityDao.setNoOfTotalWeight(
                            modbusEntityDao.getNoOfTotalWeight() + modbusEntityDao.getTotalWeight());
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

    public boolean updateDailyData(Hashtable<Short, DailyDataDao> dailyDataDaoHT) {
        try {
            for (Entry entry : dailyDataDaoHT.entrySet()) {
                DailyDataDao dailyDataDao = (DailyDataDao) entry.getValue();
                modbusRecordRepository.insertDataDailyEntity(dailyDataDao);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Exception in updating !!");

        }
        return false;
    }

    public boolean storeData(ModbusEntityDao modbusEntityDao) {
        try {
            modbusRecordRepository.insertDataEntity(modbusEntityDao);
            System.out.println("Record saved to database: " + modbusEntityDao);
            return true;
        } catch (Exception e) {
            System.err.println("Exception storing data: " + e.getMessage());
            return false;
        }
    }

    public boolean storePackingData(PackingEntityDao modbusEntityDao) {
        try {
            modbusRecordRepository.insertPackingData(modbusEntityDao);
            System.out.println("Packing record saved to database: " + modbusEntityDao);
            return true;
        } catch (Exception e) {
            System.err.println("Exception storing packing data: " + e.getMessage());
            return false;
        }
    }

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

                modbusRecordRepository.insertPackingData(modbusEntityDao);
                System.out.println("Packing record saved: " + modbusEntityDao);
                return true;

            }

        } catch (Exception e) {
            System.err.println("Error decoding/storing packing record: " + e.getMessage());
            return false;
        }
    }
    public void handleBatchData(ModbusEntityDao modbusEntityDao){
        int totalWeight = modbusEntityDao.getTotalWeight();
        modbusRecordRepository.getIsEndOfBatch(modbusEntityDao);
        boolean isEndOfbath = modbusEntityDao.isEndOfBatch();  //check batchIsEnd

        if(totalWeight == 0 &&  isEndOfbath){     // no need to update in db
            return;
        }
        else if(totalWeight == 0 && !isEndOfbath){
            modbusEntityDao.setEndOfBatch(true);
            modbusRecordRepository.updateBatchData(modbusEntityDao);
        }
        else if(isEndOfbath){
            modbusEntityDao.setEndOfBatch(false);
            modbusRecordRepository.insertNewBatchData(modbusEntityDao);
        }
        else{
            modbusEntityDao.setEndOfBatch(false);
            modbusRecordRepository.updateBatchData(modbusEntityDao);
        }


       
        

    }
}
