package com.example.modbusapplication.Controller;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Model.ExportRequestDTO;
import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Model.ModbusGroupedBatchResponse;
import com.example.modbusapplication.Repository.FlowRepository;
import com.example.modbusapplication.Service.FlowReportService;
import com.example.modbusapplication.Service.PackingReportService;

@RestController
@RequestMapping("/report")
public class ReportController {


    @Autowired
    private FlowReportService flowService;
    @Autowired
    private PackingReportService packingService;
    @Autowired
    FlowRepository flowRepository;

  @PostMapping("/fetch")
    public ResponseEntity<?> fetchReport(@RequestBody ModbusDataRequestDAO requestDAO) {
        try {
            Short deviceId = requestDAO.getDeviceId();
            if (deviceId == null) {
                return ResponseEntity.badRequest().body("Device ID must be provided");
            }

            boolean packingMachine = flowRepository.isPackingMachine(deviceId);

            Object response;
            if (packingMachine) {
                response = packingService.flowReportExcel(requestDAO);
            } else {
                response = flowService.flowReport(requestDAO);
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/export-excel")
    public ResponseEntity<InputStreamResource> exportGroupedBatch(@RequestBody ExportRequestDTO requestDTO) throws Exception {
     String deviceName = requestDTO.getDeviceName();
    String startDate = requestDTO.getStartDate();
    String endDate = requestDTO.getEndDate();
    ModbusGroupedBatchResponse response = flowService.getGroupedData(requestDTO);
    InputStream excelStream = flowService.exportGroupedBatchToExcel(response, requestDTO.getDeviceName());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate startDatetime = LocalDate.parse(startDate, formatter);
    LocalDate endDatetime   = LocalDate.parse(endDate, formatter);
    // Format file name
        String formattedStart = startDatetime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String formattedEnd = endDatetime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName = deviceName + "_" + formattedStart + "_to_" + formattedEnd + ".xlsx";


    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
    // headers.add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(excelStream));
}


}
