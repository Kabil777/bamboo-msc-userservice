package com.bamboo.userService.dto.feign;

import java.util.UUID;

public record AuthorSummaryV1Dto(UUID id, String name, String handle, String avatarUrl) {}
