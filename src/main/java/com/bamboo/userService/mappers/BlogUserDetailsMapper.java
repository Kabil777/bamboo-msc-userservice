package com.bamboo.userService.mappers;

import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.entity.UserModel;

import java.util.function.Function;

public final class BlogUserDetailsMapper {
    private BlogUserDetailsMapper() {}

    public static final Function<UserModel, BlogMetaDto> mapUser =
            user ->
                    new BlogMetaDto(
                            user.getId(),
                            user.getName(),
                            user.getHandle(),
                            user.getCoverUrl(),
                            user.getEmail());
}
