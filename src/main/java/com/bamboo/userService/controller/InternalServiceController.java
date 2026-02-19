package com.bamboo.userService.controller;

import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalServiceController {
    private final UserService userService;

    @GetMapping("/user")
    public ResponseEntity<BlogMetaDto> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email);
    }
}
