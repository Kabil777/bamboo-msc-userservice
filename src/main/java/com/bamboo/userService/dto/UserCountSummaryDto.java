package com.bamboo.userService.dto;

import com.bamboo.userService.common.model.VisibilityCount;

import java.util.Map;

public record UserCountSummaryDto(
        long followers,
        long following,
        VisibilityCount blogs,
        VisibilityCount docs,
        Map<String, Long> otherCounts) {}
