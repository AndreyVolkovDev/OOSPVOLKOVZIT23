package com.example.cachingapi.controller;

import com.example.cachingapi.model.ResultData;
import com.example.cachingapi.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results") // Базовый URL для всех эндпоинтов в этом контроллере
public class DataController {

    @Autowired
    private DataService dataService;

    /**
     * API для загрузки данных.
     * Метод: POST, URL: /api/results
     * Тело запроса: {"content": "какие-то данные", "readOnly": true}
     */
    @PostMapping
    public ResponseEntity<ResultData> uploadResult(@RequestBody ResultData resultData) {
        ResultData savedData = dataService.uploadData(resultData);
        return ResponseEntity.ok(savedData);
    }

    /**
     * API для выгрузки результата по ID.
     * Метод: GET, URL: /api/results/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResultData> getResultById(@PathVariable Long id) {
        ResultData data = dataService.getDataById(id);
        return ResponseEntity.ok(data);
    }

    /**
     * API для выгрузки полного отчета (всех результатов).
     * Метод: GET, URL: /api/results/report
     */
    @GetMapping("/report")
    public ResponseEntity<List<ResultData>> getReport() {
        List<ResultData> allData = dataService.getAllDataForReport();
        return ResponseEntity.ok(allData);
    }

    /**
     * API для обновления данных.
     * Метод: PUT, URL: /api/results/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResultData> updateResult(@PathVariable Long id, @RequestBody ResultData resultData) {
        ResultData updatedData = dataService.updateData(id, resultData);
        return ResponseEntity.ok(updatedData);
    }
}