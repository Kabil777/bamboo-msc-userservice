package com.bamboo.userService.service;

import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.dto.ProvisionUserRequest;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.mappers.BlogUserDetailsMapper;
import com.bamboo.userService.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final UserAvatarStorageService userAvatarStorageService;
    private final HandleService handleService;

    public UserProvisioningService(
            UserRepository userRepository,
            UserAvatarStorageService userAvatarStorageService,
            HandleService handleService) {
        this.userRepository = userRepository;
        this.userAvatarStorageService = userAvatarStorageService;
        this.handleService = handleService;
    }

    public BlogMetaDto provisionUser(ProvisionUserRequest request) {
        String resolvedName = resolveName(request.name(), request.email(), request.id());
        String resolvedCoverUrl =
                userAvatarStorageService.storeExternalAvatar(request.coverUrl(), request.id());
        UserModel userModel =
                userRepository
                        .findById(request.id())
                        .orElseGet(
                                () ->
                                        UserModel.builder()
                                                .id(request.id())
                                                .name(resolvedName)
                                                .email(request.email())
                                                .handle(handleService.buildProvisionHandle(request))
                                                .description("")
                                                .coverUrl(
                                                        resolvedCoverUrl != null
                                                                        && !resolvedCoverUrl.isBlank()
                                                                ? resolvedCoverUrl
                                                                : null)
                                                .build());

        userModel.setEmail(request.email());
        userModel.setName(resolvedName);

        if (userModel.getHandle() == null || userModel.getHandle().isBlank()) {
            userModel.setHandle(handleService.buildProvisionHandle(request));
        }

        if ((userModel.getCoverUrl() == null || userModel.getCoverUrl().isBlank())
                && resolvedCoverUrl != null
                && !resolvedCoverUrl.isBlank()) {
            userModel.setCoverUrl(resolvedCoverUrl);
        }

        userRepository.save(userModel);
        return BlogUserDetailsMapper.mapUser.apply(userModel);
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
}
