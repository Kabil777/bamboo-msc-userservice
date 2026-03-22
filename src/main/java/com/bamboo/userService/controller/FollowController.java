package com.bamboo.userService.controller;

import com.bamboo.userService.dto.FollowUserDto;
import com.bamboo.userService.dto.UserCountSummaryDto;
import com.bamboo.userService.service.FollowService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/users")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("{userId}/follow")
    public ResponseEntity<Map<String, String>> addFollower(
            @PathVariable("userId") UUID followingId, @RequestHeader("X-User-Id") UUID followerId) {
        Map<String, String> result = followService.followUser(followerId, followingId);
        HttpStatus status =
                "created".equals(result.get("status")) ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(result);
    }

    @DeleteMapping("{userId}/follow")
    public ResponseEntity<Map<String, String>> removeFollower(
            @PathVariable("userId") UUID followingId, @RequestHeader("X-User-Id") UUID followerId) {
        return ResponseEntity.ok(followService.unfollowUser(followerId, followingId));
    }

    @PostMapping("handle/{handle}/follow")
    public ResponseEntity<Map<String, String>> addFollowerByHandle(
            @PathVariable("handle") String handle, @RequestHeader("X-User-Id") UUID followerId) {
        Map<String, String> result = followService.followUserByHandle(followerId, handle);
        HttpStatus status =
                "created".equals(result.get("status")) ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(result);
    }

    @DeleteMapping("handle/{handle}/follow")
    public ResponseEntity<Map<String, String>> removeFollowerByHandle(
            @PathVariable("handle") String handle, @RequestHeader("X-User-Id") UUID followerId) {
        return ResponseEntity.ok(followService.unfollowUserByHandle(followerId, handle));
    }

    @GetMapping("{handle}/followers")
    public ResponseEntity<List<FollowUserDto>> getFollowers(@PathVariable String handle) {
        return ResponseEntity.ok(followService.getFollowersByHandle(handle));
    }

    @GetMapping("{handle}/following")
    public ResponseEntity<List<FollowUserDto>> getFollowing(@PathVariable String handle) {
        return ResponseEntity.ok(followService.getFollowingByHandle(handle));
    }

    @GetMapping("{handle}/counts")
    public ResponseEntity<UserCountSummaryDto> getCounts(
            @PathVariable String handle,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        return ResponseEntity.ok(followService.getCountsByHandle(handle, requesterId));
    }
}
