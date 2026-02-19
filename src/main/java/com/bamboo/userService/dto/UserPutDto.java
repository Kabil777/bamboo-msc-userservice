package com.bamboo.userService.dto;

import com.bamboo.userService.common.enums.Designation;
import com.bamboo.userService.common.model.UserProfile;

import java.util.Optional;

public record UserPutDto(
        Optional<String> name,
        Optional<String> handle,
        Optional<String> description,
        Optional<String> coverUrl,
        Optional<Designation> designation,
        Optional<UserProfile> userProfile) {}
