package com.example.modbusapplication.Repository;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.modbusapplication.Model.ModbusEntityDao;

@Repository
public class ModbusRecordRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Creating a table
    public void createTable(String deviceId) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS modbus_data_" + deviceId.trim() + "(" +
                "timestamp DATETIME NOT NULL," +
                "batch_name  VARCHAR(15)," +
                "set_weight  INT," +
                "actual_weight INT," +
                "total_weight INT)";
        System.out.println("sql === " + sql);
        jdbcTemplate.execute(sql);
    }

    public void insertDataEntity(ModbusEntityDao modbusEntityDao) {
        try {
            String sql = "INSERT INTO modbus_data_" + modbusEntityDao.getDeviceId()

                    + "(timestamp, batch_name, set_weight, actual_weight, total_weight) VALUES (?, ?, ?, ? ,?)";

            jdbcTemplate.update(sql,
                    modbusEntityDao.getTimestamp(),
                    modbusEntityDao.getBatchName(),
                    modbusEntityDao.getSetWeight(),
                    modbusEntityDao.getActualWeight(),
                    modbusEntityDao.getTotalWeight());

        } catch (Exception e) {
            System.out.println(
                    "Exception on insertDataEntity :: DeviceID ::" + modbusEntityDao.getDeviceId() + " :: " + e);
        }
    }

}
