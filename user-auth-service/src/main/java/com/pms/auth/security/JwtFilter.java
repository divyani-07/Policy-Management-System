package com.pms.auth.security;

import com.pms.auth.repository.UserRepository;
import com.pms.auth.repository.TokenBlacklistRepository;
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
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // ✅ Inject blacklist repository
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Extract token
        String token = authHeader.substring(7);


        if (tokenBlacklistRepository.existsByToken(token)) {
            log.warn("[JwtFilter] Blacklisted token used");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json");

            response.getWriter().write("{\"message\": \"Token invalidated. Please login again.\"}");
            return; // STOP request here
        }

        try {
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                var user = userRepository.findByUsername(username).orElse(null);

                if (user != null && jwtService.validateToken(token, username)) {

                    String role = jwtService.extractRole(token);

                    var authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[JwtFilter] Authenticated user: {}, role: {}", username, role);
                }
            }

        } catch (Exception e) {
            log.warn("[JwtFilter] Invalid token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}