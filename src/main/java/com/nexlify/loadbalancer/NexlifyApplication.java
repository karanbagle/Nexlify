package com.nexlify.loadbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableAsync
@EnableWebSocket
@EnableScheduling
public class NexlifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexlifyApplication.class, args);
	}

}
