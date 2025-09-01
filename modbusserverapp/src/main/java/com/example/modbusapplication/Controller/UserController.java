package com.example.modbusapplication.Controller;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.modbusapplication.Model.ExportRequestDTO;
import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Model.ModbusGroupedBatchResponse;
import com.example.modbusapplication.Service.ExcelExportService;
import com.example.modbusapplication.Service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private ExcelExportService excelExportService;

	@PostMapping("/data")
	public ResponseEntity<?> getModbusData(@RequestBody ModbusDataRequestDAO requestDAO) {
		try {
			Object response = userService.fetchModbusDataFlexible(requestDAO);
			return ResponseEntity.ok(response); // Will return either:
												// - List<ModbusEntityDao>
												// - ModbusGroupedBatchResponse
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", "Something went wrong"));
		}
	}

	@PostMapping("/export-excel")
	public ResponseEntity<InputStreamResource> exportGroupedBatch(@RequestBody ExportRequestDTO requestDTO)
			throws Exception {
		String deviceName = requestDTO.getDeviceName();
		String startDate = requestDTO.getStartDate();
		String endDate = requestDTO.getEndDate();
		ModbusGroupedBatchResponse response = userService.getGroupedData(requestDTO);
		InputStream excelStream = excelExportService.exportGroupedBatchToExcel(response, requestDTO.getDeviceName());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate startDatetime = LocalDate.parse(startDate, formatter);
		LocalDate endDatetime = LocalDate.parse(endDate, formatter);
		// Format file name
		String formattedStart = startDatetime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		String formattedEnd = endDatetime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		String fileName = deviceName + "_" + formattedStart + "_to_" + formattedEnd + ".xlsx";

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		// headers.add("Content-Disposition", "attachment; filename=\"" + fileName +
		// "\"");

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(excelStream));
	}

}
