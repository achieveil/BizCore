package com.achieveil.bizcore.filter;

import com.achieveil.bizcore.entity.UserSession;
import com.achieveil.bizcore.security.JwtService;
import com.achieveil.bizcore.service.UserSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractToken(request);

            if (StringUtils.hasText(jwt) && !jwtService.isTokenExpired(jwt)) {
                String sessionId = jwtService.extractSessionId(jwt);
                if (StringUtils.hasText(sessionId)) {
                    Optional<UserSession> sessionOpt = userSessionService.findActiveSession(sessionId);
                    if (sessionOpt.isPresent()) {
                        UserSession session = sessionOpt.get();
                        Long tokenUserId = jwtService.extractUserId(jwt);
                        if (!session.getUserId().equals(tokenUserId)) {
                            filterChain.doFilter(request, response);
                            return;
                        }
                        String username = jwtService.extractUsername(jwt);
                        String role = jwtService.extractRole(jwt);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        userSessionService.touchSession(session);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to process JWT authentication", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
