package com.example.multitenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MultiTenantNotesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiTenantNotesApplication.class, args);
	}

}
