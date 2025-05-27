package com.S209.yobi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsyn
public class YobiApplication {

	public static void main(String[] args) {
		SpringApplication.run(YobiApplication.class, args);
	}

}
