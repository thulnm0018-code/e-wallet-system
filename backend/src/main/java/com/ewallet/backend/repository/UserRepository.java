package com.ewallet.backend.repository;

import com.ewallet.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailOrPhone(String email, String phone);

    List<User> findByDeletedFalse();

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByPhoneAndDeletedFalse(String phone);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);  

    boolean existsByPhoneAndDeletedFalse(String phone);

    long countByDeletedFalse();
}

