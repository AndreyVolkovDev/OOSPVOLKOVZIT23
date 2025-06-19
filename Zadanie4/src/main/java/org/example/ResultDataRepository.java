package com.example.cachingapi.repository;

import com.example.cachingapi.model.ResultData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Сообщает Spring, что это компонент для работы с данными
public interface ResultDataRepository extends JpaRepository<ResultData, Long> {
    // Ничего писать не нужно, Spring Data JPA предоставляет все основные методы
    // (findById, save, findAll, deleteById и т.д.) "из коробки".
}