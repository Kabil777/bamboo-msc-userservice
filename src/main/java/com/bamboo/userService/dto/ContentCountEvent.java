package com.bamboo.userService.dto;

import com.bamboo.userService.common.enums.Visibility;

import java.util.UUID;

public record ContentCountEvent(
        UUID userId,
        String contentType,
        String action,
        Visibility oldVisibility,
        Visibility newVisibility) {}
