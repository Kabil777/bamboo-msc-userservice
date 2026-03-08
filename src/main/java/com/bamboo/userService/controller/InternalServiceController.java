package com.bamboo.userService.controller;

import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.dto.ProvisionUserRequest;
import com.bamboo.userService.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalServiceController {
    private final UserService userService;

    @GetMapping(value = "/user", params = "email")
    public ResponseEntity<BlogMetaDto> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping(value = "/user/{id}")
    public ResponseEntity<BlogMetaDto> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PostMapping("/user/provision")
    public ResponseEntity<BlogMetaDto> provisionUser(@RequestBody ProvisionUserRequest request) {
        return userService.provisionUser(request);
    }
}
