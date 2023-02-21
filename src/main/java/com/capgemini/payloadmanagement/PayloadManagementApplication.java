package com.capgemini.payloadmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Rohithkumar Senthilkumar
 */

@SpringBootApplication
@EnableEurekaClient
public class PayloadManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayloadManagementApplication.class, args);
	}

}
