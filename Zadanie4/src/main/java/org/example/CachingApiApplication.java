package com.example.cachingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // <-- Эта аннотация включает механизм кэширования во всем приложении
public class CachingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CachingApiApplication.class, args);
        System.out.println("\nПриложение успешно запущено!");
        System.out.println("API доступно по адресу: http://localhost:8080");
        System.out.println("Консоль БД H2: http://localhost:8080/h2-console");
        System.out.println(" (JDBC URL: jdbc:h2:mem:testdb, User Name: sa, Password: password)");
    }
}