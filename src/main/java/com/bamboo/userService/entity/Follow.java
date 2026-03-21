package com.bamboo.userService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "followers",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_follower_following",
                        columnNames = {"followerId", "followingId"}),
        indexes = {
            @Index(name = "ix_followers_follower", columnList = "followerId"),
            @Index(name = "ix_followers_following", columnList = "followingId")
        })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Follow {
    @Id @GeneratedValue private UUID id;

    @Column(nullable = false)
    private UUID followerId;

    @Column(nullable = false)
    private UUID followingId;
}
