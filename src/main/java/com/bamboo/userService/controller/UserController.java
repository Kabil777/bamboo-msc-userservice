package com.bamboo.userService.controller;

import com.bamboo.userService.dto.UserMetaDto;
import com.bamboo.userService.dto.UserPostDto;
import com.bamboo.userService.dto.UserProfileDto;
import com.bamboo.userService.dto.UserPutDto;
import com.bamboo.userService.common.enums.Visibility;
import com.bamboo.userService.dto.feign.BlogCursorResponseV1Dto;
import com.bamboo.userService.dto.feign.DocCursorResponseV1Dto;
import com.bamboo.userService.feign.PostServiceClient;
import com.bamboo.userService.service.HandleService;
import com.bamboo.userService.service.UserProfileService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserProfileService userProfileService;
    private final HandleService handleService;
    private final PostServiceClient postServiceClient;

    public UserController(
            UserProfileService userProfileService,
            HandleService handleService,
            PostServiceClient postServiceClient) {
        this.userProfileService = userProfileService;
        this.handleService = handleService;
        this.postServiceClient = postServiceClient;
    }

    @GetMapping("/meta")
    public ResponseEntity<UserMetaDto> getUserMeta(@RequestHeader("X-User-Id") UUID id) {
        return ResponseEntity.ok(userProfileService.getUserMeta(id));
    }

    @PostMapping("/meta")
    public ResponseEntity<Map<String, String>> createUser(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody UserPostDto userModel) {
        return ResponseEntity.ok(userProfileService.saveData(userId, userModel));
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestHeader("X-User-Id") UUID userId, @RequestBody UserPutDto userModel) {
        return ResponseEntity.ok(userProfileService.update(userId, userModel));
    }

    @GetMapping("/profile/me")
    public ResponseEntity<UserProfileDto> getProfileBlogs(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @GetMapping("/profile/{handle}")
    public ResponseEntity<UserProfileDto> getProfileByHandle(@PathVariable String handle) {
        return ResponseEntity.ok(userProfileService.getProfileByHandle(handle));
    }

    @GetMapping("/profile/me/blogs")
    public ResponseEntity<BlogCursorResponseV1Dto> getProfileBlog(
            @RequestHeader("X-User-Id") UUID id, @RequestParam(required = false) Instant cursor) {
        BlogCursorResponseV1Dto response = postServiceClient.getBlogByUser(id, cursor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{handle}/blogs")
    public ResponseEntity<BlogCursorResponseV1Dto> getProfileBlogByHandle(
            @PathVariable String handle, @RequestParam(required = false) Instant cursor) {
        UUID userId = handleService.getUserByHandle(handle).getId();
        BlogCursorResponseV1Dto response =
                postServiceClient.getBlogByUserVisibility(userId, cursor, Visibility.PUBLIC);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/me/docs")
    public ResponseEntity<DocCursorResponseV1Dto> getProfileDocs(
            @RequestHeader("X-User-Id") UUID id, @RequestParam(required = false) Instant cursor) {
        DocCursorResponseV1Dto response = postServiceClient.getDocsByUser(id, cursor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{handle}/docs")
    public ResponseEntity<DocCursorResponseV1Dto> getProfileDocsByHandle(
            @PathVariable String handle, @RequestParam(required = false) Instant cursor) {
        UUID userId = handleService.getUserByHandle(handle).getId();
        DocCursorResponseV1Dto response =
                postServiceClient.getDocsByUserVisibility(userId, cursor, Visibility.PUBLIC);
        return ResponseEntity.ok(response);
    }
}
