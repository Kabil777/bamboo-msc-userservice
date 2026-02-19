package com.bamboo.userService.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserTag {
    DEVELOPER,
    DESIGNER,
    WRITER,
    PHOTOGRAPHER,
    CREATOR,
    ARTIST,
    ENGINEER,
    ENTREPRENEUR,
    STUDENT,
    TEACHER,
    MANAGER,
    FREELANCER,
    PRODUCT_MANAGER,
    DATA_SCIENTIST,
    DEVOPS,
    QA_ENGINEER,
    BACKEND,
    FRONTEND,
    FULL_STACK,
    MOBILE_DEV,
    UI_UX,
    CONTENT_CREATOR;

    @JsonCreator
    public static UserTag from(String value) {
        return UserTag.valueOf(value.toUpperCase().replace(" ", "_").replace("/", "_"));
    }

    @JsonValue
    public String toValue() {
        return name();
    }
};
