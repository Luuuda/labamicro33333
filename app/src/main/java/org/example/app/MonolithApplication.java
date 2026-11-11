package org.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.example") // Кажемо Спрінгу шукати контролери у всіх наших модулях
public class MonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonolithApplication.class, args);
    }
}