package com.pms.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration      // This class contains Spring configuration (Bean definitions)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;  // Our custom filter from File 8

    @Bean  // Spring will call this method and store the result as a managed bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                // Disable CSRF — not needed for REST APIs (CSRF is for browser form submissions)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // STATELESS = don't create HTTP sessions
                // JWT is our session — every request must carry the token itself

                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/api/auth/login").permitAll()
                                // /api/auth/login is PUBLIC — no token needed (obviously, you need to log in first!)

                                .requestMatchers("/api/users/**").hasRole("ADMIN")
                                // Only ADMIN can access user management endpoints

                                .anyRequest().authenticated()
                        // Every other endpoint requires a valid token
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        // Add our JwtFilter to run BEFORE Spring's default login filter
        // So every request goes: JwtFilter → Controller

        return http.build();
    }
}
