package com.pms.auth.security;

import com.pms.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component            // Spring manages this as a bean
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
// OncePerRequestFilter = runs exactly once per HTTP request (not multiple times)

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {

        // Step 1: Read the "Authorization" header from the request
        // Header looks like: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        String authHeader = request.getHeader("Authorization");

        // Step 2: If no Authorization header OR doesn't start with "Bearer " → skip
        // (This request might be going to /api/auth/login which is public — no token needed)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // Pass to next filter
            return;  // Stop processing here
        }

        // Step 3: Extract just the token part (remove "Bearer " prefix)
        // "Bearer eyJhbGci..." → "eyJhbGci..."
        String token = authHeader.substring(7);

        try {
            // Step 4: Get username from the token
            String username = jwtService.extractUsername(token);

            // Step 5: Only proceed if username found AND user not already authenticated
            // SecurityContextHolder = Spring's current session holder for this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Load user from DB
                var user = userRepository.findByUsername(username).orElse(null);

                // Step 7: Validate token is genuine and not expired
                if (user != null && jwtService.validateToken(token, username)) {

                    String role = jwtService.extractRole(token);

                    // Step 8: Create authentication object for Spring Security
                    // "ROLE_ADMIN" format is required by Spring Security
                    var authToken = new UsernamePasswordAuthenticationToken(
                            username,     // principal (who is logged in)
                            null,         // credentials (null — we don't need password here)
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            // authorities = what this user can access
                    );

                    // Step 9: Tell Spring Security "this request is authenticated"
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[JwtFilter] Authenticated user: {}, role: {}", username, role);
                }
            }

        } catch (Exception e) {
            // Token is invalid/tampered/expired → log and continue (Spring Security will block it)
            log.warn("[JwtFilter] Invalid token: {}", e.getMessage());
        }

        // Step 10: Continue to the actual Controller
        filterChain.doFilter(request, response);
    }
}
