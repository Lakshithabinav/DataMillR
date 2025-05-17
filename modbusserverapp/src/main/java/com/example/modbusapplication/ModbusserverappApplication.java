package com.example.modbusapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModbusserverappApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModbusserverappApplication.class, args);
	}

}
