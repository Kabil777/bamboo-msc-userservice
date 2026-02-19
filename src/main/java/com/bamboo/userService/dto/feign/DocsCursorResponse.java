package com.bamboo.userService.dto.feign;

import java.time.Instant;
import java.util.List;

public record DocsCursorResponse(List<DocHomeDto> docs, Boolean hasNext, Instant cursor) {}
