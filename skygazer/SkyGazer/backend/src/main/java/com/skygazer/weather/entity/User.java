package com.skygazer.weather.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    private Long id;
    
    private String username;
    
    private String password;
    
    private String email;
    
    private String phone;
    
    private String nickname;
    
    private String avatar;
    
    private String defaultLocation;
    
    private String userProfile;
    
    @Builder.Default
    private String preferredTheme = "auto";
    
    @Builder.Default
    private Boolean notificationEnabled = true;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    @Builder.Default
    private Boolean isActive = true;
}
