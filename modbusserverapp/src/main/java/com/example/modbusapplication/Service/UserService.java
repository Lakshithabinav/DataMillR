package com.example.modbusapplication.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.modbusapplication.Model.ModbusDataRequestDAO;
import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Repository.ModbusRecordRepository;
@Service
public class UserService {

    
   @Autowired
   ModbusRecordRepository modbusRecordRepository;

   public List<ModbusEntityDao> fetchModbusData(ModbusDataRequestDAO requestDAO) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    Short deviceId = requestDAO.getDeviceId();
    String startDateStr = requestDAO.getStartDate();
    String endDateStr = requestDAO.getEndDate();

    if (deviceId == null) {
        throw new IllegalArgumentException("Device ID must be provided");
    }

    LocalDateTime start;
    LocalDateTime end;

    try {
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);
            start = startDate.atStartOfDay();
            end = endDate.atTime(23, 59, 59);
;
            int total_weight = 0;
            List<ModbusEntityDao> modbusEntityDaos = modbusRecordRepository.fetchDataBtwnDates(""+start,""+end,""+deviceId);

            List<ModbusEntityDao> modbusEntityDaosRes = new ArrayList<>();
            ModbusEntityDao modbusEntityDaoTemp = new ModbusEntityDao();

            String batchName = null;
            for(ModbusEntityDao modbusEntityDao : modbusEntityDaos)
            {
                if(modbusEntityDao.getBatchName().equals(batchName))
                {
                    modbusEntityDaoTemp.setEndDateTime(modbusEntityDao.getEndDateTime());
                    modbusEntityDaoTemp.setBatchName(modbusEntityDao.getBatchName());
                    modbusEntityDaoTemp.setSetWeight(modbusEntityDao.getSetWeight());
                    modbusEntityDaoTemp.setActualWeight(modbusEntityDao.getActualWeight());  
                    modbusEntityDao.setActualWeight(modbusEntityDao.getTotalWeight()-total_weight);
                    total_weight = modbusEntityDao.getTotalWeight();
                }
                else
                {
                    modbusEntityDaosRes.add(modbusEntityDaoTemp);
                    modbusEntityDaoTemp = new ModbusEntityDao();
                    total_weight =  modbusEntityDao.getTotalWeight();
                    modbusEntityDaoTemp.setTimestamp(modbusEntityDao.getTimestamp());       
                }
                return modbusEntityDaosRes;
            }

        } else if (startDateStr != null || endDateStr != null) {
            LocalDate singleDate = LocalDate.parse(startDateStr != null ? startDateStr : endDateStr, formatter);
            start = singleDate.atStartOfDay();
            end = singleDate.atTime(23, 59, 59);
        } else {
            // No date provided, get all records for the device
            return modbusRecordRepository.getAllDataByDeviceId(deviceId);
        }
    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd");
    }

    return modbusRecordRepository.getDataByDeviceIdAndDateRange(deviceId, start, end);
}


}
