package com.bamboo.userService.controller;

import com.bamboo.userService.dto.UserMetaDto;
import com.bamboo.userService.dto.UserPostDto;
import com.bamboo.userService.dto.UserProfileDto;
import com.bamboo.userService.dto.UserPutDto;
import com.bamboo.userService.dto.feign.CursorResponse;
import com.bamboo.userService.feign.PostServiceClient;
import com.bamboo.userService.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final PostServiceClient postServiceClient;

    public UserController(UserService userService, PostServiceClient postServiceClient) {
        this.userService = userService;
        this.postServiceClient = postServiceClient;
    }

    @GetMapping("/meta")
    public ResponseEntity<UserMetaDto> getUserMeta(@RequestHeader("X-User-Id") UUID id) {
        return userService.getUserMeta(id);
    }

    @PostMapping("/meta")
    public ResponseEntity<Map<String, String>> createUser(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Name") String name,
            @RequestHeader("X-User-Handle") String handle,
            @RequestHeader("X-User-Avatar") String avatar,
            @RequestBody UserPostDto userModel) {
        return userService.saveData(userId, email, name, handle, avatar, userModel);
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateProfile(
            @RequestHeader("X-User-Id") UUID userId, @RequestBody UserPutDto userModel) {
        return userService.update(userId, userModel);
    }

    @GetMapping("/profile/me")
    public ResponseEntity<UserProfileDto> getProfileBlogs(@RequestHeader("X-User-Id") UUID userId) {
        return userService.getProfile(userId);
    }

    @GetMapping("/profile/me/blogs")
    public ResponseEntity<CursorResponse> getProfileBlog(
            @RequestHeader("X-User-Id") UUID id, @RequestParam(required = false) Instant cursor) {
        CursorResponse response = postServiceClient.getBlogByUser(id, cursor);
        return ResponseEntity.ok(response);
    }
}
