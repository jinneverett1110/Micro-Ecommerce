package quant.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.authservice.client.UserServiceGrpcClient;
import quant.authservice.config.JwtConfig;
import quant.authservice.dto.request.LoginRequest;
import quant.authservice.dto.request.RegisterRequest;
import quant.authservice.dto.response.AuthResponse;
import quant.authservice.entity.UserEntity;
import quant.authservice.exception.AppException;
import quant.authservice.exception.ErrorCode;
import quant.authservice.repository.UserRepository;
import quant.authservice.util.JwtUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final UserServiceGrpcClient userServiceGrpcClient;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        if (userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserEntity.Role.USER)
                .build();

        String refreshToken = generateRefreshToken();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(
                Instant.now().plusSeconds(jwtConfig.getRefreshTokenExpiration() / 1000)
        );

        UserEntity savedUser = userRepository.save(user);

        userServiceGrpcClient.createUserProfile(savedUser.getId(), savedUser.getEmail());

        return buildAuthResponse(user, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.isActive())
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);

        if (user.isLocked())
            throw new AppException(ErrorCode.ACCOUNT_LOCKED,
                    "Tài khoản bị khóa đến " + user.getLockedUntil());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());

        String refreshToken = generateRefreshToken();
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(
                Instant.now().plusSeconds(jwtConfig.getRefreshTokenExpiration() / 1000)
        );

        userRepository.save(user);

        return buildAuthResponse(user, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        UserEntity user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (user.getRefreshTokenExpiry().isBefore(Instant.now()))
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);

        String newRefreshToken = generateRefreshToken();
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(
                Instant.now().plusSeconds(jwtConfig.getRefreshTokenExpiration() / 1000)
        );

        userRepository.save(user);

        return buildAuthResponse(user, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        UserEntity user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
    }

    private void handleFailedLogin(UserEntity user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS)
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES));

        userRepository.save(user);
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    private AuthResponse buildAuthResponse(UserEntity user, String refreshToken) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getAccessTokenExpiration())
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }
}
