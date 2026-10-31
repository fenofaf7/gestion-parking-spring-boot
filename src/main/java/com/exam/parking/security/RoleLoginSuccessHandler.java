
package com.exam.parking.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleLoginSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        String destination;

        if (hasRole(authentication, "ROLE_ADMIN")) {
            destination = "/admin/dashboard";

        } else if (hasRole(authentication, "ROLE_MANAGER")) {
            destination = "/manager/dashboard";

        } else if (hasRole(authentication, "ROLE_OPERATEUR")) {
            destination = "/operateur/dashboard";

        } else if (hasRole(authentication, "ROLE_CLIENT")) {
            destination = "/client/dashboard";

        } else {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Rôle non autorisé"
            );
            return;
        }

        response.sendRedirect(
            request.getContextPath() + destination
        );
    }

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
