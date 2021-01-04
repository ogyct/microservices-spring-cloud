package com.example.slowservice

import com.augy.dto.Item
import com.augy.dto.Person
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import kotlin.random.Random.Default.nextInt

@SpringBootApplication
@EnableDiscoveryClient
class SlowServiceApplication

fun main(args: Array<String>) {
    runApplication<SlowServiceApplication>(*args)
}

@RestController
class SlowController {

    @GetMapping("/random-person")
    fun slowPerson(): Mono<Person> {
        return Mono.just(Person(UUID.randomUUID().toString(), nextInt(0, 100)))
                .delayElement(Duration.ofSeconds(0))
    }

    @GetMapping("/random-item")
    fun slowItem(): Mono<Item> {
        return Mono.just(Item(UUID.randomUUID().toString(), nextInt(0, 100)))
                .delayElement(Duration.ofSeconds(1))
    }
}
