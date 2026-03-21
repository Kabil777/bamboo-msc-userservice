package com.bamboo.userService.common.model;

public record VisibilityCount(long total, long publicCount, long privateCount) {

    public static VisibilityCount empty() {
        return new VisibilityCount(0, 0, 0);
    }
}
