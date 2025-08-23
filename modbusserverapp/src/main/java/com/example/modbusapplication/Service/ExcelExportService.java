package com.example.modbusapplication.Service;

import com.example.modbusapplication.Model.ModbusBatchGroup;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.ModbusGroupedBatchResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelExportService {

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
