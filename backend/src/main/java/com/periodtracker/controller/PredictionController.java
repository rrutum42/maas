package com.periodtracker.controller;

import com.periodtracker.dto.PredictionResponse;
import com.periodtracker.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping
    public ResponseEntity<PredictionResponse> predict(@RequestParam Long userId) {
        return ResponseEntity.ok(predictionService.predict(userId));
    }
}
