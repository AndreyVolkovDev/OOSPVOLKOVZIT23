package com.example.cachingapi.service;

import com.example.cachingapi.model.ResultData;
import com.example.cachingapi.repository.ResultDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataService {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    @Autowired
    private ResultDataRepository repository;

    /**
     * Загрузка/сохранение новых данных в БД.
     */
    public ResultData uploadData(ResultData data) {
        log.info("Сохранение новых данных в БД: {}", data);
        return repository.save(data);
    }

    /**
     * Получение данных по ID.
     * Реализует требование №1: кэшировать только read-only данные.
     *
     * @Cacheable - указывает, что результат этого метода можно кэшировать.
     * value = "results" - название области кэша.
     * key = "#id" - ключ, по которому будет сохранено значение (ID объекта).
     * condition = "#result.readOnly" - САМОЕ ВАЖНОЕ! Условие на языке SpEL.
     * Кэширование сработает только в том случае, если у возвращаемого объекта (#result)
     * поле readOnly равно true.
     */
    @Cacheable(value = "results", key = "#id", condition = "#result.readOnly")
    @Transactional(readOnly = true)
    public ResultData getDataById(Long id) {
        // Это сообщение будет в логах только тогда, когда метод реально выполняется (т.е. нет кэша)
        log.info("--- ЗАПРОС К БД --- Получение данных по id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Данные с id " + id + " не найдены"));
    }

    /**
     * Обновление данных. При обновлении мы должны удалить старую запись из кэша,
     * так как ее статус readOnly или содержимое могли измениться.
     *
     * @CacheEvict - удаляет запись из кэша.
     */
    @CacheEvict(value = "results", key = "#id")
    @Transactional
    public ResultData updateData(Long id, ResultData newData) {
        log.info("Обновление данных для id: {}. Запись удалена из кэша.", id);
        ResultData existingData = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Данные с id " + id + " не найдены"));

        existingData.setContent(newData.getContent());
        existingData.setReadOnly(newData.isReadOnly());

        return repository.save(existingData);
    }

    /**
     * Выгрузка всех результатов для отчета.
     * Реализует требование №2: этот метод НЕ кэшируется и всегда идет в БД.
     */
    @Transactional(readOnly = true)
    public List<ResultData> getAllDataForReport() {
        log.info("--- ЗАПРОС К БД --- Получение ВСЕХ данных для отчета.");
        return repository.findAll();
    }
}