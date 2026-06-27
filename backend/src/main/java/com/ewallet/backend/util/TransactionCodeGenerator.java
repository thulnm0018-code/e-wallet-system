package com.ewallet.backend.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionCodeGenerator {

    public String generate() {

        return "TX"
                + System.currentTimeMillis()
                + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }       
}