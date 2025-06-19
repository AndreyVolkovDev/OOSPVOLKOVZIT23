package com.example.cachingapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity // Указывает, что этот класс является сущностью для БД
@Data   // Lombok: автоматически создает геттеры, сеттеры, toString(), equals() и hashCode()
@NoArgsConstructor // Lombok: создает пустой конструктор, необходимый для JPA
public class ResultData implements Serializable { // Serializable важен для кэширования

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content; // Содержимое данных

    private boolean readOnly; // Ключевой флаг для логики кэширования
}