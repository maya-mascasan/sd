package com.andrei.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath(); // Use getServletPath() for consistency        String method = request.getMethod();

        // 1. Explicitly skip EVERYTHING for these paths
        if (path.startsWith("/login") || path.startsWith("/password-reset")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Handle OPTIONS (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // If no token is provided, just continue.
        // SecurityConfig will decide if the path requires authentication or not.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtService.isTokenValid(token)) {
                log.warn("Token is expired or invalid for path: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            // Standardize role to "ROLE_ADMIN", "ROLE_STUDENT", etc.
            // This is critical for .hasRole("ADMIN") to work!
            String formattedRole = role.startsWith("ROLE_") ?
                    role.toUpperCase() :
                    "ROLE_" + role.toUpperCase();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority(formattedRole))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Successfully authenticated user {} with role {}", email, formattedRole);

        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
            // We don't block the chain here; we let SecurityConfig handle the 403
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // This tells Spring: "If the URL starts with these, don't look for a JWT"
        return path.startsWith("/login") ||
                path.startsWith("/auth/") ||
                path.startsWith("/password-reset/");
    }
}