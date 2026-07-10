package com.skygazer.weather.service;

import com.skygazer.weather.dto.request.UserLoginRequest;
import com.skygazer.weather.dto.request.UserRegisterRequest;
import com.skygazer.weather.dto.response.AuthResponse;

public interface AuthService {
    
    AuthResponse register(UserRegisterRequest request);
    
    AuthResponse login(UserLoginRequest request);
    
    String generateToken(String username);
    
    boolean validateToken(String token);
}
