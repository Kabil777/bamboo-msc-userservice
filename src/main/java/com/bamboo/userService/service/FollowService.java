package com.bamboo.userService.service;

import com.bamboo.userService.dto.FollowUserDto;
import com.bamboo.userService.dto.UserCountSummaryDto;
import com.bamboo.userService.entity.Follow;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.mappers.FollowUserMapper;
import com.bamboo.userService.repository.FollowRepository;
import com.bamboo.userService.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final HandleService handleService;
    private final UserCountService userCountService;

    public FollowService(
            FollowRepository followRepository,
            UserRepository userRepository,
            HandleService handleService,
            UserCountService userCountService) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.handleService = handleService;
        this.userCountService = userCountService;
    }

    @Transactional
    public Map<String, String> followUser(UUID follower, UUID following) {
        if (follower.equals(following)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        if (!userRepository.existsById(following)) {
            throw new EntityNotFoundException("User not found");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(follower, following)) {
            return Map.of("status", "already_following");
        }

        Follow newFollowRequest =
                Follow.builder().followerId(follower).followingId(following).build();
        followRepository.save(newFollowRequest);
        userCountService.incrementFollow(follower, following);
        return Map.of("status", "created");
    }

    @Transactional
    public Map<String, String> unfollowUser(UUID follower, UUID following) {
        if (follower.equals(following)) {
            throw new IllegalArgumentException("You cannot unfollow yourself");
        }

        if (!followRepository.existsByFollowerIdAndFollowingId(follower, following)) {
            return Map.of("status", "not_following");
        }

        followRepository.deleteByFollowerIdAndFollowingId(follower, following);
        userCountService.decrementFollow(follower, following);
        return Map.of("status", "deleted");
    }

    public List<FollowUserDto> getFollowersByHandle(String handle) {
        UUID userId = getUserIdByHandle(handle);
        List<UUID> followerIds =
                followRepository.findAllByFollowingId(userId).stream()
                        .map(Follow::getFollowerId)
                        .toList();
        return mapUsersByIdsInOrder(followerIds);
    }

    public List<FollowUserDto> getFollowingByHandle(String handle) {
        UUID userId = getUserIdByHandle(handle);
        List<UUID> followingIds =
                followRepository.findAllByFollowerId(userId).stream()
                        .map(Follow::getFollowingId)
                        .toList();
        return mapUsersByIdsInOrder(followingIds);
    }

    public UserCountSummaryDto getCountsByHandle(String handle, UUID requesterId) {
        return userCountService.getSummaryByHandleForViewer(handle, requesterId);
    }

    private UUID getUserIdByHandle(String handle) {
        return userRepository
                .findByHandleIgnoreCase(handleService.normalizeHandle(handle))
                .map(UserModel::getId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private List<FollowUserDto> mapUsersByIdsInOrder(List<UUID> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, FollowUserDto> usersById =
                userRepository.findAllById(userIds).stream()
                        .collect(
                                Collectors.toMap(
                                        UserModel::getId,
                                        FollowUserMapper.mapUser,
                                        (left, right) -> left));

        return userIds.stream().map(usersById::get).filter(Objects::nonNull).toList();
    }
}
