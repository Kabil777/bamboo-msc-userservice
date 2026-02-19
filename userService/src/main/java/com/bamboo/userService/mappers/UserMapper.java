package com.bamboo.userService.mappers;

import com.bamboo.userService.dto.UserProfileDto;
import com.bamboo.userService.entity.UserModel;

import java.util.function.Function;

public final class UserMapper {

    private UserMapper() {}

    public static final Function<UserModel, UserProfileDto> mapProfile =
            user ->
                    new UserProfileDto(
                            user.getName(),
                            user.getHandle(),
                            user.getEmail(),
                            user.getDescription(),
                            user.getCoverUrl(),
                            user.getDesignation(),
                            user.getUserProfile());
}
