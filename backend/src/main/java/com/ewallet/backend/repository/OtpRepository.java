package com.ewallet.backend.repository;

import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import com.ewallet.backend.enums.OtpPurpose;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByUserOrderByCreatedAtDesc(User user);

    Optional<Otp> findTopByUserAndPurposeOrderByCreatedAtDesc(User user, OtpPurpose purpose);

    Optional<Otp> findTopByUser_EmailOrderByCreatedAtDesc(String email);

    Optional<Otp> findTopByUser_PhoneOrderByCreatedAtDesc(String phone);

    Optional<Otp> findTopByUser_EmailOrUser_PhoneOrderByCreatedAtDesc(
            String email,
            String phone
    );

        Optional<Otp> findTopByUser_EmailAndPurposeOrUser_PhoneAndPurposeOrderByCreatedAtDesc(
            String email, OtpPurpose emailPurpose, String phone, OtpPurpose phonePurpose);

    List<Otp> findByUser(User user);

    void deleteAllByUser(User user);
}