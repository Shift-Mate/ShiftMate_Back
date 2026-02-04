package com.example.shiftmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableJpaAuditing
public class ShiftMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShiftMateApplication.class, args);
	}

}
