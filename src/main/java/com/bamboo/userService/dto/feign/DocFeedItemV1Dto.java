package com.bamboo.userService.dto.feign;

import com.bamboo.userService.common.enums.PostStatus;
import com.bamboo.userService.common.enums.Visibility;

import java.time.Instant;
import java.util.UUID;

public record DocFeedItemV1Dto(
        UUID id,
        String title,
        String coverUrl,
        String description,
        Instant createdAt,
        Visibility visibility,
        PostStatus status,
        AuthorSummaryV1Dto author) {}
