package com.bamboo.userService.feign;

import com.bamboo.userService.dto.feign.CursorResponse;
import com.bamboo.userService.dto.feign.DocsCursorResponse;
import com.bamboo.userService.common.enums.Visibility;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.UUID;

@FeignClient(name = "post-service-client", url = "${blog.service.url}")
public interface PostServiceClient {
    @GetMapping("/internal/blogs/user/{id}")
    CursorResponse getBlogByUser(
            @PathVariable UUID id, @RequestParam(required = false) Instant cursor);

    @GetMapping("/api/v1/blog/user/{id}")
    CursorResponse getBlogByUserVisibility(
            @PathVariable UUID id,
            @RequestParam(required = false) Instant cursor,
            @RequestParam(required = false) Visibility visibility);

    @GetMapping("/internal/docs/user/{id}")
    DocsCursorResponse getDocsByUser(
            @PathVariable UUID id, @RequestParam(required = false) Instant cursor);

    @GetMapping("/api/v1/docs/user/{id}")
    DocsCursorResponse getDocsByUserVisibility(
            @PathVariable UUID id,
            @RequestParam(required = false) Instant cursor,
            @RequestParam(required = false) Visibility visibility);
}
