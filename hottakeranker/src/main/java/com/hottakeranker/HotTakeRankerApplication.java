package com.hottakeranker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotTakeRankerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotTakeRankerApplication.class, args);
	}

}
