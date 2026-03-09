package com.bamboo.userService.dto.feign;

import com.bamboo.userService.common.enums.PostStatus;
import com.bamboo.userService.common.enums.Visibility;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BlogFeedItemV1Dto(
        UUID id,
        String title,
        String coverUrl,
        String description,
        List<String> tags,
        Instant createdAt,
        Visibility visibility,
        PostStatus status,
        AuthorSummaryV1Dto author,
        List<AuthorSummaryV1Dto> collaborators) {}
