package com.bamboo.userService.service;

import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.entity.UserModel;
import com.bamboo.userService.mappers.BlogUserDetailsMapper;
import com.bamboo.userService.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserLookupService {

    private final UserRepository userRepository;

    public UserLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public BlogMetaDto getUserByEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim();

        BlogMetaDto userDetails =
                userRepository
                        .findByEmailIgnoreCase(normalizedEmail)
                        .map(BlogUserDetailsMapper.mapUser)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userDetails;
    }

    public BlogMetaDto getUserById(UUID id) {
        BlogMetaDto userDetails =
                userRepository
                        .findById(id)
                        .map(BlogUserDetailsMapper.mapUser)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userDetails;
    }

    public List<BlogMetaDto> getUsersByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllById(ids).stream().map(BlogUserDetailsMapper.mapUser).toList();
    }

    public UserModel getUserEntityByHandle(String handle) {
        return userRepository
                .findByHandleIgnoreCase(handle)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
