package com.bamboo.userService.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumeratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "bookmark")
public class Bookmark {
    @Id private UUID blogId;
    @EnumeratedValue private String type;
    UUID userId;
}
