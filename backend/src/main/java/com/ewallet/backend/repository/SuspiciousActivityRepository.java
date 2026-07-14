package com.ewallet.backend.repository;

import com.ewallet.backend.entity.SuspiciousActivity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspiciousActivityRepository
        extends JpaRepository<SuspiciousActivity, Long> {

    List<SuspiciousActivity>
    findAllByOrderByDetectedAtDesc();
}