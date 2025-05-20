package com.example.modbusapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.cache.annotation.EnableCaching;
=======
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
<<<<<<< HEAD
@EnableCaching
=======
>>>>>>> c0d921221e3f0f6eb7148554f2064c5abe9b61a0
public class ModbusserverappApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModbusserverappApplication.class, args);
	}

}
