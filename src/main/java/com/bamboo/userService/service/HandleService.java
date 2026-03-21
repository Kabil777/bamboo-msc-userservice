package com.bamboo.userService.service;

import com.bamboo.userService.dto.ProvisionUserRequest;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HandleService {

    private final UserRepository userRepository;

    public HandleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String normalizeHandle(String handle) {
        if (handle == null) {
            return "";
        }
        return handle.toLowerCase().replace(" ", "_");
    }

    public String buildProvisionHandle(ProvisionUserRequest request) {
        String preferred = request.handle();
        if (preferred == null || preferred.isBlank()) {
            preferred = request.name();
        }
        if (preferred == null || preferred.isBlank()) {
            preferred = request.email() != null ? request.email().split("@")[0] : "user";
        }

        String normalized = preferred.toLowerCase().replace(" ", "_").replaceAll("[^a-z0-9_]", "");
        if (normalized.isBlank()) {
            normalized = "user";
        }

        return normalized + "_" + request.id().toString().substring(0, 4).toLowerCase();
    }

    public UserModel getUserByHandle(String handle) {
        String normalized = normalizeHandle(handle);
        return userRepository
                .findByHandleIgnoreCase(normalized)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public String resolveRequestedHandle(String handle, String name, String email, UUID userId) {
        String normalized = normalizeHandle(handle);
        if (!normalized.isBlank()) {
            return normalized;
        }

        ProvisionUserRequest request = new ProvisionUserRequest(userId, email, name, null, handle);
        return buildProvisionHandle(request);
    }
}
