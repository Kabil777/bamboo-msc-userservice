package com.bamboo.userService.mappers;

import com.bamboo.userService.dto.FollowUserDto;
import com.bamboo.userService.entity.UserModel;

import java.util.function.Function;

public final class FollowUserMapper {
    private FollowUserMapper() {}

    public static final Function<UserModel, FollowUserDto> mapUser =
            user -> new FollowUserDto(user.getId(), user.getName(), user.getHandle(), user.getCoverUrl());
}
