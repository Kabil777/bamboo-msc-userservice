package com.bamboo.userService.dto;

import java.util.UUID;

public record ProfileUpdatedEvent(UUID id, String name, String handle, String coverUrl) {}
