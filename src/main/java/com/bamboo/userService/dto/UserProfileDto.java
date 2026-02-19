package com.bamboo.userService.dto;

import com.bamboo.userService.common.enums.Designation;
import com.bamboo.userService.common.model.UserProfile;

public record UserProfileDto(
        String name,
        String handle,
        String email,
        String description,
        String coverUrl,
        Designation designation,
        UserProfile profile) {}
