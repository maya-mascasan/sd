package com.andrei.demo.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Public & Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/password-reset/**").permitAll()
                        //.requestMatchers(HttpMethod.POST, "/login").permitAll()
                        //.requestMatchers(HttpMethod.POST, "/password-reset/**").permitAll()

                        // 2. Student Enrollment (Must be BEFORE general person management)
                        .requestMatchers(HttpMethod.POST, "/person/*/enroll/*").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "/person/*/enroll/*").hasRole("STUDENT")

                        // 3. Specific Person Access
                        .requestMatchers(HttpMethod.GET, "/person/email/**").hasAnyRole("ADMIN", "STUDENT", "PROFESSOR")

                        // 4. Person & Department Management (ADMIN ONLY)
                        .requestMatchers("/person/**").hasRole("ADMIN")
                        .requestMatchers("/department/**").hasRole("ADMIN")

                        // 5. Courses & Assignments
                        .requestMatchers(HttpMethod.GET, "/course/**").hasAnyRole("ADMIN", "PROFESSOR", "STUDENT")
                        .requestMatchers("/course/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/assignment/**").hasAnyRole("ADMIN", "PROFESSOR", "STUDENT")
                        .requestMatchers("/assignment/**").hasAnyRole("ADMIN", "PROFESSOR")

                        // 6. Submissions
                        .requestMatchers(HttpMethod.GET, "/submission/**").hasAnyRole("ADMIN", "PROFESSOR", "STUDENT")
                        .requestMatchers(HttpMethod.POST, "/submission").hasRole("STUDENT")
                        .requestMatchers("/submission/**").hasAnyRole("ADMIN", "PROFESSOR")

                        // 7. Catch-all
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}