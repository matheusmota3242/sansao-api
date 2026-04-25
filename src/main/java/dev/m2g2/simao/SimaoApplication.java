package dev.m2g2.simao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class   SimaoApplication {

	static void main(String[] args) {
		SpringApplication.run(SimaoApplication.class, args);
	}

}
