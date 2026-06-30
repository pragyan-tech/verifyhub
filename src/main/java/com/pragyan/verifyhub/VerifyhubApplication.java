package com.pragyan.verifyhub;

import com.pragyan.verifyhub.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class VerifyhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(VerifyhubApplication.class, args);
	}
}