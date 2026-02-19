package com.bamboo.userService.dto;

import java.util.UUID;

public record BlogMetaDto(UUID id, String name, String handle, String coverUrl, String email) {}
