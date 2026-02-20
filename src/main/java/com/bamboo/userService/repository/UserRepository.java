package com.bamboo.userService.repository;

import com.bamboo.userService.entity.UserModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, UUID> {

    public Optional<UserModel> findByEmailIgnoreCase(String email);

    public Optional<UserModel> findByHandleIgnoreCase(String handle);

    public boolean existsByHandle(String handle);

    public boolean existsByHandleAndIdNot(String handle, UUID id);
}
