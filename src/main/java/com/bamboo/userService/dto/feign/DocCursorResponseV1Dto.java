package com.bamboo.userService.dto.feign;

import java.time.Instant;
import java.util.List;

public record DocCursorResponseV1Dto(List<DocFeedItemV1Dto> items, Boolean hasNext, Instant cursor) {}
