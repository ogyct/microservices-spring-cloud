package com.example.fastService;

import com.augy.dto.Item;
import com.augy.dto.ItemPerson;
import com.augy.dto.Person;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;


@SpringBootApplication
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
	private final ReactiveCircuitBreakerFactory cbFactory;

	FastServiceController(WebClient.Builder builder, ReactiveCircuitBreakerFactory cbFactory) {
		this.builder = builder;
		this.cbFactory = cbFactory;
	}

	@GetMapping("/")
	String hello() {
		return appName + " is working";
	}

	@GetMapping("/person")
	Mono<Person> getPerson() {
		return builder.build().get().uri("http://slow-service/random-person").retrieve().bodyToMono(Person.class)
				.transform(it -> cbFactory.create("slow").run(it, t -> Mono.just(new Person())));
	}

	@GetMapping("/itemPerson")
	Mono<ItemPerson> itemPerson() {
		Mono<Person> personMono = builder.build().get().uri("/random-person").retrieve().bodyToMono(Person.class);
		Mono<Item> itemMono = builder.build().get().uri("/random-item").retrieve().bodyToMono(Item.class);
		return Mono.zip(personMono, itemMono, (person, item) -> ItemPerson.builder().item(item).person(person).build());
	}
}

@Configuration
class Config {
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		CircuitBreakerRegistry cbr = CircuitBreakerRegistry.ofDefaults();
		return factory -> {
			factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
					.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
					.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
					.circuitBreakerConfig(CircuitBreakerConfig.custom().failureRateThreshold(10)
							.slowCallRateThreshold(5).slowCallRateThreshold(2).build()).build());
		};
	}

	@Bean
	@LoadBalanced
	WebClient.Builder builder() {
		return WebClient.builder().baseUrl("http://slow-service");
	}
}
