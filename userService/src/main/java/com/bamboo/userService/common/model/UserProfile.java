package com.bamboo.userService.common.model;

import com.bamboo.userService.common.enums.SocialPlatform;
import com.bamboo.userService.common.enums.UserTag;

import java.util.Map;
import java.util.Set;

public record UserProfile(Set<UserTag> tags, Map<SocialPlatform, String> social) {}
