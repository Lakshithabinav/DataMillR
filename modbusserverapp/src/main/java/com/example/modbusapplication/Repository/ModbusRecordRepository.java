package com.example.modbusapplication.Repository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.modbusapplication.Model.ModbusEntityDao;
import com.example.modbusapplication.Model.PackingEntityDao;

@Repository
public class ModbusRecordRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Creating a table
    public void createTable(String deviceId) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS modbus_data_" + deviceId.trim() + "(" +
                "timestamp DATETIME NOT NULL," +
                "status  INT," +
                "batch_name  VARCHAR(15)," +
                "set_weight  INT," +
                "present_weight INT," +
                "total_weight INT)";
        System.out.println("sql === " + sql);
        jdbcTemplate.execute(sql);
    }

    public void packing_createTable(String deviceId) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS packing_data_" + deviceId.trim() + " (" +
            "timestamp DATETIME NOT NULL," +
            "batch_id VARCHAR(50)," +
            "bag_weight_set INT," +
            "actual_weight INT," +
            "scale_used VARCHAR(50)," +
            "bag_count INT," +
            "accumulated_weight INT," +
            "machine_status VARCHAR(10)" +
            ")";
        System.out.println("SQL === " + sql);
        jdbcTemplate.execute(sql);
    }


    public void insertDataEntity(ModbusEntityDao modbusEntityDao) {
        String sql = "INSERT INTO modbus_data_" + modbusEntityDao.getDeviceId()
            + " (timestamp, status, batch_name, set_weight, present_weight, total_weight) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
            modbusEntityDao.getTimestamp(),
            modbusEntityDao.getStatus(),
            modbusEntityDao.getBatchName(),
            modbusEntityDao.getSetWeight(),
            modbusEntityDao.getPresentWeight(),
            modbusEntityDao.getTotalWeight());
    }
    public void insertPackingData(PackingEntityDao modbusEntityDao) {
        String sql = "INSERT INTO packing_data_" + modbusEntityDao.getDeviceId()
            + " (timestamp, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
            modbusEntityDao.getTimestamp(),
            modbusEntityDao.getBatchId(),
            modbusEntityDao.getBagWeightSet(),
            modbusEntityDao.getActualWeight(),
            modbusEntityDao.getScaleUsed(),
            modbusEntityDao.getBagCount(),
            modbusEntityDao.getAccumulatedWeight(),
            modbusEntityDao.getMachineStatus()
            );
    }

    
   public List<ModbusEntityDao> getDataByDeviceIdAndDateRange(short deviceId, LocalDateTime start, LocalDateTime end) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, batch_name, set_weight, present_weight, total_weight FROM " + tableName +
                 " WHERE timestamp BETWEEN ? AND ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            entity.setPresentWeight(rs.getInt("present_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
            return entity;
        }, start, end); 
    }

    public List<PackingEntityDao> getPackingDataByDeviceIdAndDateRange(short deviceId, LocalDateTime start, LocalDateTime end) {
        String tableName = "packing_data_" + deviceId;
        String sql = "SELECT timestamp, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status "
               + "FROM " + tableName + " WHERE timestamp BETWEEN ? AND ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        PackingEntityDao entity = new PackingEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setBatchId(rs.getString("batch_id"));
            entity.setBagWeightSet(rs.getInt("bag_weight_set"));
            entity.setActualWeight(rs.getInt("actual_weight"));
            entity.setScaleUsed(rs.getString("scale_used"));
            entity.setBagCount(rs.getInt("bag_count"));
            entity.setAccumulatedWeight(rs.getInt("accumulated_weight"));
            entity.setMachineStatus(rs.getString("machine_status"));
            entity.setDeviceId(deviceId);
        return entity;
        }, start, end);
    }

    public List<ModbusEntityDao> getAllDataByDeviceId(short deviceId) {
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, batch_name, set_weight, present_weight, total_weight FROM " 
               + tableName + " ORDER BY timestamp DESC Limit 1";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setStatus(rs.getInt("status"));
            entity.setBatchName(rs.getString("batch_name"));
            entity.setSetWeight(rs.getInt("set_weight"));
            entity.setPresentWeight(rs.getInt("present_weight"));
            entity.setTotalWeight(rs.getInt("total_weight"));
            entity.setDeviceId(deviceId);
        return entity;
        });
    }

    public List<PackingEntityDao> getAllPackingDataByDeviceId(short deviceId) {
        String tableName = "packing_data_" + deviceId;
        String sql = "SELECT timestamp, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status " 
               + "FROM " + tableName + " ORDER BY timestamp DESC LIMIT 1";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        PackingEntityDao entity = new PackingEntityDao();
            entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            entity.setBatchId(rs.getString("batch_id"));
            entity.setBagWeightSet(rs.getInt("bag_weight_set"));
            entity.setActualWeight(rs.getInt("actual_weight"));
            entity.setScaleUsed(rs.getString("scale_used"));
            entity.setBagCount(rs.getInt("bag_count"));
            entity.setAccumulatedWeight(rs.getInt("accumulated_weight"));
            entity.setMachineStatus(rs.getString("machine_status"));
            entity.setDeviceId(deviceId);
        return entity;
        });
    }

    public List<ModbusEntityDao> fetchDataBtwnDates(String startDate,String endDate,short deviceId){
        String tableName = "modbus_data_" + deviceId;
        String sql = "SELECT timestamp, status, batch_name, set_weight, present_weight, total_weight FROM " 
               + tableName + " where timestamp >="+startDate+" and timestamp<="+endDate;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ModbusEntityDao entity = new ModbusEntityDao();
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setStatus(rs.getInt("status"));
        entity.setBatchName(rs.getString("batch_name"));
        entity.setSetWeight(rs.getInt("set_weight"));
        entity.setPresentWeight(rs.getInt("present_weight"));
        entity.setTotalWeight(rs.getInt("total_weight"));
        entity.setDeviceId(deviceId);
        return entity;
        });        

    }

    public List<PackingEntityDao> fetchPackingDataBtwnDates(String startDate, String endDate, Short deviceId) {
    String tableName = "packing_data_" + deviceId;
    String sql = "SELECT timestamp, batch_id, bag_weight_set, actual_weight, scale_used, bag_count, accumulated_weight, machine_status "
               + "FROM " + tableName + " WHERE timestamp BETWEEN ? AND ?";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
        PackingEntityDao entity = new PackingEntityDao();
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setBatchId(rs.getString("batch_id"));
        entity.setBagWeightSet(rs.getInt("bag_weight_set"));
        entity.setActualWeight(rs.getInt("actual_weight"));
        entity.setScaleUsed(rs.getString("scale_used"));
        entity.setBagCount(rs.getInt("bag_count"));
        entity.setAccumulatedWeight(rs.getInt("accumulated_weight"));
        entity.setMachineStatus(rs.getString("machine_status"));
        entity.setDeviceId(deviceId);
        return entity;
    }, startDate, endDate);
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
