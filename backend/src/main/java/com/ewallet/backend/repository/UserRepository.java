package com.ewallet.backend.repository;

import com.ewallet.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // tim kiem user theo email, co the tra ve null neu khong tim thay
    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailOrPhone(
            String email,
            String phone
    );

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
