package com.example.fastService;

import com.augy.dto.Person;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@SpringBootApplication
@EnableDiscoveryClient
public class FastServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastServiceApplication.class, args);
	}

}

@RestController
class FastServiceController {

	private final WebClient.Builder builder;
	@Value("${spring.application.name}")
	private String appName;

	FastServiceController(WebClient.Builder builder) {
		this.builder = builder;
	}

	@GetMapping("/")
	String hello() {
		return appName + " is working";
	}

	@GetMapping("/person")
	Mono<Person> getPerson() {
		return builder.build().get().uri("http://slow-service/random-person").retrieve().bodyToMono(Person.class);
	}
}

@Configuration
class Config {
	@Bean
	@LoadBalanced
	WebClient.Builder builder() {
		return WebClient.builder();
	}
}
