package com.example.modbusapplication.Repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.modbusapplication.Model.ModbusEntityDao;


@Repository
public class FlowRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Creating a table
    public void createTable(String deviceId) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS modbus_data_" + deviceId.trim() + "(" +
                "timestamp DATETIME NOT NULL," +
                "status  INT," +
                "flowrate INT," +
                "batch_name  VARCHAR(15)," +
                "set_weight  INT," +
                // "present_weight INT," +
                "total_weight INT)";
        System.out.println("sql === " + sql);
        jdbcTemplate.execute(sql);
    }

  


    public void insertDataEntity(ModbusEntityDao modbusEntityDao) {
        String sql = "INSERT INTO modbus_data_" + modbusEntityDao.getDeviceId()
            + " (`timestamp`, status, flowrate, batch_name, set_weight, total_weight) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
            modbusEntityDao.getTimestamp(),
            modbusEntityDao.getStatus(),
            modbusEntityDao.getFlowrate(),
            modbusEntityDao.getBatchName(),
            modbusEntityDao.getSetWeight(),
            // modbusEntityDao.getPresentWeight(),
            modbusEntityDao.getTotalWeight());
    }
   
   public List<ModbusEntityDao> getDataByDeviceIdAndDateRange(short deviceId, LocalDateTime start, LocalDateTime end) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight FROM " + tableName +
                 " WHERE timestamp BETWEEN ? AND ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setFlowrate(rs.getInt("flowrate"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight")); 
            // entity.setPresentWeight(rs.getInt("present_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
            return entity;
        }, start, end); 
    }

   
        public List<ModbusEntityDao> getLastDataByDeviceId(short deviceId) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight FROM " 
               + tableName + " ORDER BY timestamp DESC Limit 1";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
        return entity;
        });
    }

public List<ModbusEntityDao> getAllDataByDeviceId(short deviceId) {
    String tableName = "modbus_data_" + deviceId;
    String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight " +
                 "FROM " + tableName + " WHERE timestamp >= ? ORDER BY timestamp ASC";

    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

    return jdbcTemplate.query(
        sql,
        (rs, rowNum) -> {
            ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setFlowrate(rs.getInt("flowrate"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            // entity.setPresentWeight(rs.getInt("present_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
            return entity;
        },
        Timestamp.valueOf(startOfToday) // passing args as varargs
    );
}

    public List<ModbusEntityDao> getDataByDeviceIdForLogin(short deviceId) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT `timestamp`, status, flowrate, batch_name, set_weight, total_weight " 
        + "FROM " + tableName + " ORDER BY `timestamp` DESC LIMIT 1";

        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setFlowrate(rs.getInt("flowrate"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            // entity.setPresentWeight(rs.getInt("present_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
            return entity;
        });
    }

   

    public List<ModbusEntityDao> fetchDataBtwnDates(String startDate,String endDate,short deviceId){
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, flowrate, batch_name, set_weight, total_weight FROM " 
               + tableName + " WHERE timestamp BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setStatus(rs.getInt("status"));
        entity.setFlowrate(rs.getInt("flowrate"));
        entity.setBatchName(rs.getString("batch_name"));
        entity.setSetWeight(rs.getInt("set_weight"));
        // entity.setPresentWeight(rs.getInt("present_weight"));
        entity.setTotalWeight(rs.getInt("total_weight"));
        entity.setDeviceId(deviceId);
        return entity;
        });        

    }

   


    public boolean isPackingMachine(Short deviceId) {
    String tableName = "packing_data_" + deviceId;
    
    try {
        // Check if the table exists
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
    } catch (Exception e) {
        // If query fails, assume it's not a packing machine
        return false;
    }
}     

}
