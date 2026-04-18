package com.pms.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity  // THIS enables @PreAuthorize annotations in controllers
// Without this, @PreAuthorize("hasRole('ADMIN')") does nothing!
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/auth/login").permitAll()
                                // Login is open to everyone

                                .requestMatchers("/api/auth/validate").permitAll()
                                // Validate is called by other microservices with their own token
                                // We do the auth check inside the service, not at filter level

                                .anyRequest().authenticated()
                        // Everything else must have a valid JWT
                        // Role checks are done by @PreAuthorize in controllers
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}