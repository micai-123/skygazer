package com.skygazer.weather.config;

import com.skygazer.weather.entity.User;
import com.skygazer.weather.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (!userMapper.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nickname("管理员")
                .email("admin@skygazer.com")
                .isActive(true)
                .build();
            userMapper.save(admin);
            log.info("Created default admin user: admin / admin123");
        }
        
        if (!userMapper.existsByUsername("test")) {
            User testUser = User.builder()
                .username("test")
                .password(passwordEncoder.encode("test123"))
                .nickname("测试用户")
                .email("test@skygazer.com")
                .isActive(true)
                .build();
            userMapper.save(testUser);
            log.info("Created default test user: test / test123");
        }
        
        log.info("Data initialization completed");
    }
}
