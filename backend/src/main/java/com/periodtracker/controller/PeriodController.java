package com.periodtracker.controller;

import com.periodtracker.dto.PeriodListResponse;
import com.periodtracker.dto.PeriodLogRequest;
import com.periodtracker.dto.PeriodLogResponse;
import com.periodtracker.service.PeriodService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/periods")
public class PeriodController {

    private static final int MAX_PAGE_SIZE = 200;

    private final PeriodService periodService;

    public PeriodController(PeriodService periodService) {
        this.periodService = periodService;
    }

    @PostMapping
    public ResponseEntity<PeriodLogResponse> log(@Valid @RequestBody PeriodLogRequest request) {
        PeriodService.PeriodLogResult result = periodService.logPeriod(request);
        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.response());
    }

    @GetMapping
    public ResponseEntity<PeriodListResponse> list(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cursor,
            @RequestParam(defaultValue = "50") int size) {
        int pageSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        return ResponseEntity.ok(periodService.listPeriods(userId, from, to, cursor, pageSize));
    }
}
