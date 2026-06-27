package com.ewallet.backend.security;

import com.ewallet.backend.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl
        implements CurrentUserService {

    @Override
    public Long getCurrentUserId() {

        var authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new UnauthorizedException(
                    "Unauthorized"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal == null
                || "anonymousUser".equals(principal)) {

            throw new UnauthorizedException(
                    "Unauthorized"
            );
        }

        try {
            return Long.parseLong(
                    principal.toString()
            );
        } catch (NumberFormatException ex) {

            throw new UnauthorizedException(
                    "Unauthorized"
            );
        }
    }
}
