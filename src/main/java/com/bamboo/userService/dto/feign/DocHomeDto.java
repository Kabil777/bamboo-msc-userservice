package com.bamboo.userService.dto.feign;

import java.time.Instant;
import java.util.UUID;

public record DocHomeDto(
        UUID id, String title, String coverUrl, String description, Instant createdAt) {}
