package com.example.shiftmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling // 스케줄러 동작 활성화
public class ShiftMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShiftMateApplication.class, args);
	}

}
