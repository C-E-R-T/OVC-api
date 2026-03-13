package com.example.ovcbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
//매월 1일마다 데이터 sync
@EnableScheduling
@SpringBootApplication
public class OvcBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OvcBackendApplication.class, args);
	}

}
