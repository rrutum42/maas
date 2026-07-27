package com.periodtracker.dto;

import java.util.List;

public record PeriodListResponse(
        List<PeriodLogResponse> data,
        Pagination pagination) {

    public record Pagination(String nextCursor, boolean hasMore) {
    }
}
