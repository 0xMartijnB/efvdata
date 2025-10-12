package com.mb.efvdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EfvdataApplication {

	public static void main(String[] args) {
		SpringApplication.run(EfvdataApplication.class, args);
	}

}
