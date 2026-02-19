package com.bamboo.userService.entity;

import com.bamboo.userService.common.enums.Designation;
import com.bamboo.userService.common.model.UserProfile;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserModel {
    @Id private UUID id;

    @Nonnull private String name;

    @Nonnull
    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false)
    private String handle;

    @Column(columnDefinition = "text")
    private String description;

    @Column(unique = true)
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    private Designation designation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    UserProfile userProfile;
}
