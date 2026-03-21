package com.bamboo.userService.service;

import com.bamboo.userService.common.helper.BlankCheck;
import com.bamboo.userService.dto.ProfileUpdatedEvent;
import com.bamboo.userService.dto.UserMetaDto;
import com.bamboo.userService.dto.UserPostDto;
import com.bamboo.userService.dto.UserProfileDto;
import com.bamboo.userService.dto.UserPutDto;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.mappers.UserMapper;
import com.bamboo.userService.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserAvatarStorageService userAvatarStorageService;
    private final HandleService handleService;
    private final RabbitTemplate rabbitTemplate;

    public UserProfileService(
            UserRepository userRepository,
            UserAvatarStorageService userAvatarStorageService,
            HandleService handleService,
            RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.userAvatarStorageService = userAvatarStorageService;
        this.handleService = handleService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public UserMetaDto getUserMeta(UUID id) {
        UserModel user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return new UserMetaDto(
                user.getName(), user.getHandle(), user.getEmail(), user.getCoverUrl());
    }

    @Transactional
    public Map<String, String> saveData(UUID userId, UserPostDto user) {
        UserModel userModel =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userModel.setDesignation(user.designation());
        userModel.setUserProfile(user.userProfile());

        userRepository.save(userModel);
        return Map.of("profile", "created successfully");
    }

    @Transactional
    public Map<String, String> update(UUID userId, UserPutDto user) {
        UserModel userModel =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String previousName = userModel.getName();
        String previousHandle = userModel.getHandle();
        String previousCoverUrl = userModel.getCoverUrl();

        BlankCheck.notBlank(user.name(), "name").ifPresent(userModel::setName);
        BlankCheck.notBlank(user.description(), "description").ifPresent(userModel::setDescription);

        BlankCheck.notBlank(user.coverUrl(), "coverUrl")
                .ifPresent(
                        coverUrl ->
                                userModel.setCoverUrl(
                                        userAvatarStorageService.storeExternalAvatar(
                                                coverUrl, userId)));

        user.designation().ifPresent(userModel::setDesignation);
        BlankCheck.notBlank(user.handle(), "handle")
                .ifPresent(
                        handle -> {
                            String newHandle = handleService.normalizeHandle(handle);
                            userModel.setHandle(newHandle);
                        });
        user.userProfile().ifPresent(userModel::setUserProfile);
        boolean projectionUpdated =
                !Objects.equals(previousName, userModel.getName())
                        || !Objects.equals(previousHandle, userModel.getHandle())
                        || !Objects.equals(previousCoverUrl, userModel.getCoverUrl());

        try {
            userRepository.saveAndFlush(userModel);
        } catch (DataIntegrityViolationException ex) {
            throw mapUniqueConstraint(ex);
        }
        if (projectionUpdated) {
            String currentName = userModel.getName();
            String currentHandle = userModel.getHandle();
            String currentCoverUrl = userModel.getCoverUrl();
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            rabbitTemplate.convertAndSend(
                                    "user.events",
                                    "profile.updated",
                                    new ProfileUpdatedEvent(
                                            userId, currentName, currentHandle, currentCoverUrl));
                        }
                    });
        }

        return Map.of("profile", "updated successfully");
    }

    public UserProfileDto getProfile(UUID id) {
        UserProfileDto profile =
                userRepository
                        .findById(id)
                        .map(UserMapper.mapProfile)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return profile;
    }

    public UserProfileDto getProfileByHandle(String handle) {
        UserProfileDto profile =
                userRepository
                        .findByHandleIgnoreCase(handleService.normalizeHandle(handle))
                        .map(UserMapper.mapProfile)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return profile;
    }

    private IllegalStateException mapUniqueConstraint(DataIntegrityViolationException ex) {
        String message = extractConstraintMessage(ex);
        if (message.contains("handle")) {
            return new IllegalStateException("Handle already taken");
        }
        if (message.contains("email")) {
            return new IllegalStateException("Email already taken");
        }
        return new IllegalStateException("Profile update violates a unique constraint");
    }

    private String extractConstraintMessage(DataIntegrityViolationException ex) {
        Throwable root = ex.getRootCause();
        if (root != null && root.getMessage() != null) {
            return root.getMessage().toLowerCase();
        }
        return ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
    }
}
