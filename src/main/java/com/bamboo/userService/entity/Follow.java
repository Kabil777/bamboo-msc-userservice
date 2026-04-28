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
                        columnNames = {"follower_id", "following_id"}),
        indexes = {
            @Index(name = "ix_followers_follower", columnList = "follower_id"),
            @Index(name = "ix_followers_following", columnList = "following_id")
        })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Follow {
    @Id @GeneratedValue private UUID id;

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "following_id", nullable = false)
    private UUID followingId;
}
