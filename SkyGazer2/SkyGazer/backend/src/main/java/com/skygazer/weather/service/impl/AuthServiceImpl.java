package com.skygazer.weather.service.impl;

import com.skygazer.weather.dto.request.UserLoginRequest;
import com.skygazer.weather.dto.request.UserRegisterRequest;
import com.skygazer.weather.dto.response.AuthResponse;
import com.skygazer.weather.entity.User;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.repository.UserRepository;
import com.skygazer.weather.service.AuthService;
import com.skygazer.weather.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    @Override
    public AuthResponse register(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .phone(request.getPhone())
            .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
            .isActive(true)
            .build();
        
        user = userRepository.save(user);
        
        String token = generateToken(user.getUsername());
        
        return buildAuthResponse(user, token);
    }
    
    @Override
    public AuthResponse login(UserLoginRequest request) {
        log.info("登录尝试 - 用户名: {}, 密码长度: {}", request.getUsername(), request.getPassword().length());
        
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> {
                log.warn("登录失败 - 用户不存在: {}", request.getUsername());
                return new BusinessException("用户名或密码错误");
            });
        
        log.info("找到用户: {}, 数据库密码哈希: {}", user.getUsername(), user.getPassword().substring(0, 20) + "...");
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败 - 密码不匹配 - 用户: {}, 输入密码: {}", user.getUsername(), request.getPassword());
            throw new BusinessException("用户名或密码错误");
        }
        
        if (!user.getIsActive()) {
            log.warn("登录失败 - 账户被禁用: {}", user.getUsername());
            throw new BusinessException("账户已被禁用");
        }
        
        log.info("登录成功 - 用户: {}", user.getUsername());
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String token = generateToken(user.getUsername());
        
        return buildAuthResponse(user, token);
    }
    
    @Override
    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(jwtExpiration / 1000)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build())
            .build();
    }
}
