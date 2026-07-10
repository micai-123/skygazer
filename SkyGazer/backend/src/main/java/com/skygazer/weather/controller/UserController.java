package com.skygazer.weather.controller;

import com.skygazer.weather.dto.request.UserLoginRequest;
import com.skygazer.weather.dto.request.UserRegisterRequest;
import com.skygazer.weather.dto.response.ApiResponse;
import com.skygazer.weather.dto.response.AuthResponse;
import com.skygazer.weather.entity.User;
import com.skygazer.weather.service.AuthService;
import com.skygazer.weather.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final AuthService authService;
    
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.success("注册成功", response);
    }
    
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }
    
    @GetMapping("/profile")
    public ApiResponse<User> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        return ApiResponse.success(user);
    }
    
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody User userUpdate) {
        User user = userService.updateUser(userDetails.getUsername(), userUpdate);
        return ApiResponse.success("更新成功", user);
    }
    
    @PutMapping("/location")
    public ApiResponse<User> updateDefaultLocation(@AuthenticationPrincipal UserDetails userDetails,
                                                   @RequestParam String location) {
        User user = userService.updateDefaultLocation(userDetails.getUsername(), location);
        return ApiResponse.success("默认位置更新成功", user);
    }
}
