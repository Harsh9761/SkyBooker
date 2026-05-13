package com.example.authService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SkybookerAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkybookerAuthServiceApplication.class, args);
	}

}
