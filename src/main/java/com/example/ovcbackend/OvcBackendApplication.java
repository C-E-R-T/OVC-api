package com.example.ovcbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class OvcBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OvcBackendApplication.class, args);
	}

}
