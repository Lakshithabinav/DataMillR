package com.example.modbusapplication.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Model.ModbusBatchGroup;
import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusGroupedBatchResponse;
import com.example.modbusapplication.Model.PackingEntityDao;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Repository.PackingRepository;

@Service
public class PackingReportService {


    @Autowired
    FlowRepository modbusRecordRepository;
    @Autowired
    PackingRepository packingRepository;

    public Object flowReportExcel(ModbusDataRequestDAO requestDAO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Short deviceId = requestDAO.getDeviceId();
        String startDateStr = requestDAO.getStartDate();
        String endDateStr = requestDAO.getEndDate();
        boolean packingMachine = modbusRecordRepository.isPackingMachine(deviceId);
        LocalDateTime start = null, end = null;
        try{
           
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59);
        } else if (startDateStr != null) {
            LocalDate date = LocalDate.parse(startDateStr, formatter);
            start = date.atStartOfDay();
            end = date.atTime(23, 59, 59);
        } else {
            return packingRepository.getAllPackingDataByDeviceId(deviceId);
           
        }

        if (packingMachine) {     
            List<PackingEntityDao> rows =
                    packingRepository.getPackingDataByDeviceIdAndDateRange(deviceId, start, end);
            return rows;
        } else {
            List<ModbusEntityDao> rows =
                    modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
            return groupRowsByBatch(rows);
        }

    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid input: " + e.getMessage(), e);
    }
}

private ModbusGroupedBatchResponse groupRowsByBatch(List<ModbusEntityDao> allRows) {
    List<ModbusBatchGroup> batchGroups = new ArrayList<>();
    ModbusBatchGroup currentGroup = null;
    String currentBatchName = null;

    for (int i = 0; i < allRows.size(); i++) {
        ModbusEntityDao row = allRows.get(i);
        double totalWeight = row.getTotalWeight();
        String batchName = row.getBatchName();

        boolean isBatchNameChanged = currentBatchName != null && !batchName.equals(currentBatchName);
        boolean isZeroInSameBatch = totalWeight == 0 && batchName.equals(currentBatchName);

        boolean isLastZeroBeforeBatchChange = false;
        if (totalWeight == 0 && (i + 1) < allRows.size()) {
            String nextBatchName = allRows.get(i + 1).getBatchName();
            if (!nextBatchName.equals(batchName)) {
                isLastZeroBeforeBatchChange = true; // skip this row
           }
        }

        // Close batch on name change or zero within same batch
        if (isBatchNameChanged || isZeroInSameBatch) {
            if (currentGroup != null && !currentGroup.getBatchdata().isEmpty()) {
                List<ModbusEntityDao> data = currentGroup.getBatchdata();
                currentGroup.setBatchStartdata(data.get(0));
                currentGroup.setBatchEnddata(findLastNonZeroTotalWeight(data));
                batchGroups.add(currentGroup);
            }
            currentGroup = null;
        }

        // Skip zero row if it’s the last one before batch name change
        if (isLastZeroBeforeBatchChange) {
            currentBatchName = batchName; // update current batch name
            continue;
        }

        if (totalWeight == 0) {
            continue; // skip mid 0 row (already handled above)
        }

        // Start new group if needed
        if (currentGroup == null) {
            currentGroup = new ModbusBatchGroup();
            currentGroup.setBatchdata(new ArrayList<>());
        }

        currentGroup.getBatchdata().add(row);
        currentBatchName = batchName;
    }

    // Add final group if still open
    if (currentGroup != null && !currentGroup.getBatchdata().isEmpty()) {
        List<ModbusEntityDao> data = currentGroup.getBatchdata();
        currentGroup.setBatchStartdata(data.get(0));
        currentGroup.setBatchEnddata(findLastNonZeroTotalWeight(data));
        batchGroups.add(currentGroup);
    }

    ModbusGroupedBatchResponse response = new ModbusGroupedBatchResponse();
    response.setBatch(batchGroups);
    return response;
}
private ModbusEntityDao findLastNonZeroTotalWeight(List<ModbusEntityDao> batchList) {
    int i = batchList.size() - 1;

    // Step 1: Skip trailing zero totalWeights
    while (i >= 0 && batchList.get(i).getTotalWeight() == 0) {
        i--;
    }

    // Step 2: Return the last non-zero totalWeight entry
    if (i >= 0) {
        return batchList.get(i);
    }

    // Step 3: All totalWeights are zero, fallback to last row
    return batchList.get(batchList.size() - 1);
}

}
