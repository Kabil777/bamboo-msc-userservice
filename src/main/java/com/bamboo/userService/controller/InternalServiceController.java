package com.bamboo.userService.controller;

import com.bamboo.userService.dto.BlogMetaDto;
import com.bamboo.userService.dto.ContentCountEvent;
import com.bamboo.userService.dto.ProvisionUserRequest;
import com.bamboo.userService.service.UserCountService;
import com.bamboo.userService.service.UserLookupService;
import com.bamboo.userService.service.UserProvisioningService;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalServiceController {
    private final UserLookupService userLookupService;
    private final UserProvisioningService userProvisioningService;
    private final UserCountService userCountService;

    @GetMapping(value = "/user", params = "email")
    public ResponseEntity<BlogMetaDto> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userLookupService.getUserByEmail(email));
    }

    @GetMapping(value = "/user/{id}")
    public ResponseEntity<BlogMetaDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userLookupService.getUserById(id));
    }

    @PostMapping("/user/provision")
    public ResponseEntity<BlogMetaDto> provisionUser(@RequestBody ProvisionUserRequest request) {
        return ResponseEntity.ok(userProvisioningService.provisionUser(request));
    }

    @RabbitListener(queues = "user.queue.created")
    public void provisionUserQueueHandler(ProvisionUserRequest request) {
        userProvisioningService.provisionUser(request);
    }

    @RabbitListener(queues = "queue.user.content.counts")
    public void syncUserContentCounts(ContentCountEvent event) {
        userCountService.applyContentCountEvent(event);
    }
}
