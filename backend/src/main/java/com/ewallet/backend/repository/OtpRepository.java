package com.ewallet.backend.repository;

import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByUserOrderByCreatedAtDesc(User user);

    Optional<Otp> findTopByUser_EmailOrderByCreatedAtDesc(String email);

    Optional<Otp> findTopByUser_PhoneOrderByCreatedAtDesc(String phone);

    Optional<Otp> findTopByUser_EmailOrUser_PhoneOrderByCreatedAtDesc(
            String email,
            String phone
    );

    List<Otp> findByUser(User user);

    void deleteAllByUser(User user);
}