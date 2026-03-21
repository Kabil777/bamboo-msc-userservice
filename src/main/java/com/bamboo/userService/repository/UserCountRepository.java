package com.bamboo.userService.repository;

import com.bamboo.userService.entity.UserCount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserCountRepository extends JpaRepository<UserCount, UUID> {}
