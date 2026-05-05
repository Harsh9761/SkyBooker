package com.example.eurekaServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class SkybookerEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkybookerEurekaServerApplication.class, args);
	}

}
