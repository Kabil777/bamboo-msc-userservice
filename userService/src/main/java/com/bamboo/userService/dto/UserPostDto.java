package com.bamboo.userService.dto;

import com.bamboo.userService.common.enums.Designation;
import com.bamboo.userService.common.model.UserProfile;

public record UserPostDto(Designation designation, UserProfile userProfile) {}
