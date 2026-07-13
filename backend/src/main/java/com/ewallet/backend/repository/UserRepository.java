package com.ewallet.backend.repository;

import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    long countByUserStatusAndDeletedFalse(UserStatus status);

            @Query("""
            SELECT u
            FROM User u
            WHERE u.deleted = false
            AND (
                    :keyword IS NULL
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR u.phone LIKE CONCAT('%', :keyword, '%')
            )
            AND (
                    :status IS NULL
                    OR u.userStatus = :status
            )
        """)
        Page<User> searchUsers(
                @Param("keyword") String keyword,
                @Param("status") UserStatus status,
                Pageable pageable
);
}

