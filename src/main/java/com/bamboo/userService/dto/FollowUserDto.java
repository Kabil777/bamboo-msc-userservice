package com.bamboo.userService.dto;

import java.util.UUID;

public record FollowUserDto(UUID id, String name, String handle, String coverUrl) {}
