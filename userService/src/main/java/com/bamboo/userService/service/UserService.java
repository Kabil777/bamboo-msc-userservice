package com.bamboo.userService.service;

import com.bamboo.userService.common.helper.BlankCheck;
import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.dto.UserMetaDto;
import com.bamboo.userService.dto.UserPostDto;
import com.bamboo.userService.dto.UserProfileDto;
import com.bamboo.userService.dto.UserPutDto;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.exception.customExceptions.DuplicateResourceException;
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
    public ResponseEntity<Map<String, String>> saveData(
            UUID userId,
            String email,
            String name,
            String handle,
            String avatar,
            UserPostDto user) {

        UserModel existingUser = userRepository.findById(userId).orElse(null);

        if (existingUser != null) {
            throw new DuplicateResourceException("User already exists with id: " + userId);
        }

        handle = handle.toLowerCase().replace(" ", "_");
        UserModel userModel =
                UserModel.builder()
                        .id(userId)
                        .name(name)
                        .handle(handle)
                        .email(email)
                        .coverUrl(avatar)
                        .description("")
                        .designation(user.designation())
                        .userProfile(user.userProfile())
                        .build();

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
        return handle.toLowerCase().replace(" ", "_");
    }

    public ResponseEntity<UserProfileDto> getProfile(UUID id) {
        UserProfileDto profile =
                userRepository
                        .findById(id)
                        .map(UserMapper.mapProfile)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return ResponseEntity.ok(profile);
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
}
