package com.bamboo.userService.entity;

import com.bamboo.userService.common.model.VisibilityCount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_counts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCount {
    @Id private UUID userId;

    @Builder.Default
    @Column(nullable = false)
    private long followersCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private long followingCount = 0L;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blog_counts", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private VisibilityCount blogCounts = VisibilityCount.empty();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "docs_counts", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private VisibilityCount docsCounts = VisibilityCount.empty();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "other_counts", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Long> otherCounts = new HashMap<>();
}
