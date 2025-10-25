package com.achieveil.bizcore.service;

import com.achieveil.bizcore.config.JwtProperties;
import com.achieveil.bizcore.dto.LoginRequest;
import com.achieveil.bizcore.dto.LoginResponse;
import com.achieveil.bizcore.dto.RefreshTokenRequest;
import com.achieveil.bizcore.entity.Employee;
import com.achieveil.bizcore.entity.User;
import com.achieveil.bizcore.entity.UserSession;
import com.achieveil.bizcore.repository.EmployeeRepository;
import com.achieveil.bizcore.repository.UserRepository;
import com.achieveil.bizcore.security.JwtService;
import com.achieveil.bizcore.util.PasswordPolicy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserSessionService userSessionService;

    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        if (user.getStatus() != 1) {
            throw new BadCredentialsException("账户已被禁用");
        }

        Long empId = null;
        Long deptId = null;
        if (isStaff(user)) {
            Optional<Employee> employeeOpt = employeeRepository.findByUserId(user.getId());
            if (employeeOpt.isPresent()) {
                Employee employee = employeeOpt.get();
                empId = employee.getId();
                deptId = employee.getDeptId();
            }
        }

        LocalDateTime refreshExpiry = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenTtl());
        String refreshToken = jwtService.generateRefreshToken();
        UserSession session = userSessionService.createSession(
                user.getId(),
                resolveDeviceId(loginRequest),
                refreshToken,
                refreshExpiry,
                request.getHeader("User-Agent"),
                request.getRemoteAddr()
        );

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                session.getId()
        );

        return buildResponse(user, accessToken, refreshToken, session.getId(), empId, deptId);
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        UserSession session = userSessionService.findActiveSession(request.getSessionId())
                .orElseThrow(() -> new BadCredentialsException("无效的会话，请重新登录"));

        if (!userSessionService.matches(session, request.getRefreshToken())) {
            userSessionService.revokeSession(session.getId());
            throw new BadCredentialsException("刷新令牌无效或已过期");
        }

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        Long empId = null;
        Long deptId = null;
        if (isStaff(user)) {
            Optional<Employee> employeeOpt = employeeRepository.findByUserId(user.getId());
            if (employeeOpt.isPresent()) {
                Employee employee = employeeOpt.get();
                empId = employee.getId();
                deptId = employee.getDeptId();
            }
        }

        String newRefreshToken = jwtService.generateRefreshToken();
        LocalDateTime newExpiry = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenTtl());
        userSessionService.rotateRefreshToken(session, newRefreshToken, newExpiry);

        String newAccessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                session.getId()
        );

        return buildResponse(user, newAccessToken, newRefreshToken, session.getId(), empId, deptId);
    }

    public void logout(RefreshTokenRequest request) {
        userSessionService.findActiveSession(request.getSessionId())
                .filter(session -> userSessionService.matches(session, request.getRefreshToken()))
                .ifPresent(session -> userSessionService.revokeSession(session.getId()));
    }

    private void authenticate(String username, String rawPassword) {
        PasswordPolicy.ensureWithinSupportedLength(rawPassword);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPassword)
            );
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("密码长度超过当前账号支持的范围，请重置密码");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    private boolean isStaff(User user) {
        return "EMPLOYEE".equals(user.getRole()) || "MANAGER".equals(user.getRole());
    }

    private String resolveDeviceId(LoginRequest request) {
        return StringUtils.hasText(request.getDeviceId()) ? request.getDeviceId() : "unknown-device";
    }

    private LoginResponse buildResponse(User user,
                                        String accessToken,
                                        String refreshToken,
                                        String sessionId,
                                        Long empId,
                                        Long deptId) {
        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .expiresIn(jwtService.getAccessTokenTtlSeconds())
                .refreshTokenExpiresIn(jwtService.getRefreshTokenTtlSeconds())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .userId(user.getId())
                .empId(empId)
                .deptId(deptId)
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdTime(user.getCreatedTime())
                .build();
    }
}
