package com.bamboo.userService.repository;

import com.bamboo.userService.entity.Follow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    List<Follow> findAllByFollowingId(UUID followingId);

    List<Follow> findAllByFollowerId(UUID followerId);

    long countByFollowingId(UUID followingId);

    long countByFollowerId(UUID followerId);
}
