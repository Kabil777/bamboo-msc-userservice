package com.bamboo.userService.service;

import com.bamboo.userService.common.helper.BlankCheck;
import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.dto.ProvisionUserRequest;
import com.bamboo.userService.dto.UserMetaDto;
import com.bamboo.userService.dto.UserPostDto;
import com.bamboo.userService.dto.UserProfileDto;
import com.bamboo.userService.dto.UserPutDto;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.mappers.BlogUserDetailsMapper;
import com.bamboo.userService.mappers.UserMapper;
import com.bamboo.userService.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<UserMetaDto> getUserMeta(UUID id) {
        UserModel user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(
                new UserMetaDto(
                        user.getName(), user.getHandle(), user.getEmail(), user.getCoverUrl()));
    }

    @Transactional
    public ResponseEntity<Map<String, String>> saveData(UUID userId, UserPostDto user) {
        UserModel userModel =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userModel.setDesignation(user.designation());
        userModel.setUserProfile(user.userProfile());

        userRepository.save(userModel);
        return ResponseEntity.ok(Map.of("profile", "created successfully"));
    }

    @Transactional
    public ResponseEntity<Map<String, String>> update(UUID userId, UserPutDto user) {
        UserModel userModel =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found"));

        BlankCheck.notBlank(user.name(), "name").ifPresent(userModel::setName);

        BlankCheck.notBlank(user.description(), "description").ifPresent(userModel::setDescription);

        BlankCheck.notBlank(user.coverUrl(), "coverUrl").ifPresent(userModel::setCoverUrl);

        user.designation().ifPresent(userModel::setDesignation);

        BlankCheck.notBlank(user.handle(), "handle")
                .ifPresent(
                        handle -> {
                            String normalizedHandle = normalizeHandle(handle);
                            if (userRepository.existsByHandleAndIdNot(normalizedHandle, userId)) {
                                throw new IllegalStateException("Handle already taken");
                            }
                            userModel.setHandle(normalizedHandle);
                        });

        user.userProfile().ifPresent(userModel::setUserProfile);

        userRepository.save(userModel);

        return ResponseEntity.ok(Map.of("profile", "updated successfully"));
    }

    private String normalizeHandle(String handle) {
        if (handle == null) {
            return "";
        }
        return handle.toLowerCase().replace(" ", "_");
    }

    @Transactional
    public ResponseEntity<BlogMetaDto> provisionUser(ProvisionUserRequest request) {
        String resolvedName = resolveName(request.name(), request.email(), request.id());
        UserModel userModel =
                userRepository
                        .findById(request.id())
                        .orElseGet(
                                () ->
                                        UserModel.builder()
                                                .id(request.id())
                                                .name(resolvedName)
                                                .email(request.email())
                                                .handle(buildProvisionHandle(request))
                                                .description("")
                                                .coverUrl(
                                                        request.coverUrl() != null
                                                                        && !request.coverUrl()
                                                                                .isBlank()
                                                                ? request.coverUrl()
                                                                : null)
                                                .build());

        userModel.setEmail(request.email());
        userModel.setName(resolvedName);

        if (userModel.getHandle() == null || userModel.getHandle().isBlank()) {
            userModel.setHandle(buildProvisionHandle(request));
        }

        if ((userModel.getCoverUrl() == null || userModel.getCoverUrl().isBlank())
                && request.coverUrl() != null
                && !request.coverUrl().isBlank()) {
            userModel.setCoverUrl(request.coverUrl());
        }

        userRepository.save(userModel);
        return ResponseEntity.ok(BlogUserDetailsMapper.mapUser.apply(userModel));
    }

    public ResponseEntity<UserProfileDto> getProfile(UUID id) {
        UserProfileDto profile =
                userRepository
                        .findById(id)
                        .map(UserMapper.mapProfile)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return ResponseEntity.ok(profile);
    }

    public ResponseEntity<UserProfileDto> getProfileByHandle(String handle) {
        String normalized = normalizeHandle(handle);
        UserProfileDto profile =
                userRepository
                        .findByHandleIgnoreCase(normalized)
                        .map(UserMapper.mapProfile)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(profile);
    }

    public UserModel getUserByHandle(String handle) {
        String normalized = normalizeHandle(handle);
        return userRepository
                .findByHandleIgnoreCase(normalized)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public ResponseEntity<BlogMetaDto> getUserByEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim();

        BlogMetaDto userDetails =
                userRepository
                        .findByEmailIgnoreCase(normalizedEmail)
                        .map(BlogUserDetailsMapper.mapUser)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(userDetails);
    }

    public ResponseEntity<BlogMetaDto> getUserById(UUID id) {

        BlogMetaDto userDetails =
                userRepository
                        .findById(id)
                        .map(BlogUserDetailsMapper.mapUser)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(userDetails);
    }

    private String buildProvisionHandle(ProvisionUserRequest request) {
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

        String candidate = normalized + "_" + request.id().toString().substring(0, 8).toLowerCase();
        while (userRepository.existsByHandleAndIdNot(candidate, request.id())) {
            candidate = candidate + "x";
        }
        return candidate;
    }

    private String resolveName(String name, String email, UUID userId) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (email != null && !email.isBlank()) {
            return email.split("@")[0];
        }
        return "user_" + userId.toString().substring(0, 8).toLowerCase();
    }

    private String resolveRequestedHandle(String handle, String name, String email, UUID userId) {
        String normalized = normalizeHandle(handle);
        if (!normalized.isBlank()) {
            return normalized;
        }

        ProvisionUserRequest request = new ProvisionUserRequest(userId, email, name, null, handle);
        return buildProvisionHandle(request);
    }
}
