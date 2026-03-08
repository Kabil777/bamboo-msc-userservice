package com.bamboo.userService.dto;

import java.util.UUID;

public record ProvisionUserRequest(
        UUID id, String email, String name, String coverUrl, String handle) {}
