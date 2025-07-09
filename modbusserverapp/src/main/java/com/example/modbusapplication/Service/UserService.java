package com.example.modbusapplication.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Model.ModbusGroupedBatchResponse;
import com.example.modbusapplication.Model.ModbusBatchGroup;

import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Repository.ModbusRecordRepository;
@Service
public class UserService {

    
   @Autowired
   ModbusRecordRepository modbusRecordRepository;
public Object fetchModbusDataFlexible(ModbusDataRequestDAO requestDAO) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    Short deviceId = requestDAO.getDeviceId();
    String startDateStr = requestDAO.getStartDate();
    String endDateStr = requestDAO.getEndDate();

    if (deviceId == null) {
        throw new IllegalArgumentException("Device ID must be provided");
    }

    List<ModbusEntityDao> rows;

    try {
        if (startDateStr == null && endDateStr == null) {
            return modbusRecordRepository.getAllDataByDeviceId(deviceId); 
        }

        LocalDateTime start, end;

        if (startDateStr != null && endDateStr == null) {
            // ✅ deviceId + startDate
            LocalDate date = LocalDate.parse(startDateStr, formatter);
            start = date.atStartOfDay();
            end = date.atTime(23, 59, 59);
        } else if (startDateStr != null && endDateStr != null) {
            // ✅ deviceId + full date range
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59);
        } else {
            throw new IllegalArgumentException("Start date is required if end date is given");
        }

        rows = modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
        return groupRowsByBatch(rows);

    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid input: " + e.getMessage());
    }
}

private ModbusGroupedBatchResponse groupRowsByBatch(List<ModbusEntityDao> allRows) {
    List<ModbusBatchGroup> batchGroups = new ArrayList<>();

    String currentBatch = null;
    ModbusBatchGroup currentGroup = null;

    for (ModbusEntityDao row : allRows) {
        if (!row.getBatchName().equals(currentBatch)) {
            if (currentGroup != null && currentGroup.getBatchMiddledata() != null && !currentGroup.getBatchMiddledata().isEmpty()) {
                List<ModbusEntityDao> data = currentGroup.getBatchMiddledata();
                currentGroup.setBatchStartdata(data.get(0));
                currentGroup.setBatchEnddata(data.get(data.size() - 1));
                batchGroups.add(currentGroup);
            }

            currentGroup = new ModbusBatchGroup();
            currentGroup.setBatchMiddledata(new ArrayList<>());
            currentBatch = row.getBatchName();
        }

        if (currentGroup != null) {
            currentGroup.getBatchMiddledata().add(row);
        }
    }

    if (currentGroup != null && currentGroup.getBatchMiddledata() != null && !currentGroup.getBatchMiddledata().isEmpty()) {
        List<ModbusEntityDao> data = currentGroup.getBatchMiddledata();
        currentGroup.setBatchStartdata(data.get(0));
        currentGroup.setBatchEnddata(data.get(data.size() - 1));
        batchGroups.add(currentGroup);
    }

    ModbusGroupedBatchResponse response = new ModbusGroupedBatchResponse();
    response.setBatch(batchGroups);
    return response;
}

}