package com.example.modbusapplication.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.example.modbusapplication.Model.ExportRequestDTO;
import com.example.modbusapplication.Model.ModbusBatchGroup;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusGroupedBatchResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Repository.FlowRepository;

@Service
public class FlowReportService {

   @Autowired
   FlowRepository modbusRecordRepository;

    public Object flowReport(ModbusDataRequestDAO requestDAO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Short deviceId = requestDAO.getDeviceId();
        String startDateStr = requestDAO.getStartDate();
        String endDateStr = requestDAO.getEndDate();
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
        } 
            List<ModbusEntityDao> rows =
                    modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
            return groupRowsByBatch(rows);

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

public ModbusGroupedBatchResponse getGroupedData(ExportRequestDTO requestDTO) {
    ModbusDataRequestDAO dao = new ModbusDataRequestDAO();
    
    // Set required fields (all are strings)
    dao.setDeviceId(requestDTO.getDeviceId());
    dao.setStartDate(requestDTO.getStartDate());  // String like "2025-07-25"
    dao.setEndDate(requestDTO.getEndDate());

    Object result = flowReport(dao);

    if (result instanceof ModbusGroupedBatchResponse) {
        return (ModbusGroupedBatchResponse) result;
    } else {
        throw new IllegalArgumentException("Expected grouped batch response but got raw data.");
    }
}


 public InputStream exportGroupedBatchToExcel(ModbusGroupedBatchResponse groupedBatchResponse, String deviceName) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Grouped Batch Report");

        // Create bold and colored header style with borders
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Normal cell style with borders
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        // Format for datetime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        int rowNum = 0;

        // Title row
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Device Name: " + deviceName);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));

        // Header row
        Row header = sheet.createRow(rowNum++);
        String[] headers = {"S.No", "Batch Name", "Start Time", "End Time", "Total Weight"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int serialNumber = 1;
        for (ModbusBatchGroup group : groupedBatchResponse.getBatch()) {
            ModbusEntityDao start = group.getBatchStartdata();
            ModbusEntityDao end = group.getBatchEnddata();

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(serialNumber++); // S.No
            row.createCell(1).setCellValue(start.getBatchName());
            row.createCell(2).setCellValue(start.getTimestamp().format(formatter));
            row.createCell(3).setCellValue(end.getTimestamp().format(formatter));
            row.createCell(4).setCellValue(end.getTotalWeight());

            // Apply cell style to all cells
            for (int i = 0; i <= 4; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        // Auto size columns
        for (int i = 0; i <= 4; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
