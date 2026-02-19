package com.bamboo.userService.common.helper;

import java.util.Optional;

public final class BlankCheck {

    private BlankCheck() {}

    public static Optional<String> notBlank(Optional<String> value, String field) {
        if (value.isEmpty()) return Optional.empty();
        String v = value.get().trim();

        if (v.isEmpty()) {
            throw new IllegalArgumentException(field + "cannot be null");
        }
        return Optional.of(v);
    }
}
