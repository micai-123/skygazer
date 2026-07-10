package com.skygazer.weather.config;

import com.skygazer.weather.entity.User;
import com.skygazer.weather.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nickname("管理员")
                .email("admin@skygazer.com")
                .isActive(true)
                .build();
            userRepository.save(admin);
            log.info("Created default admin user: admin / admin123");
        }
        
        if (!userRepository.existsByUsername("test")) {
            User testUser = User.builder()
                .username("test")
                .password(passwordEncoder.encode("test123"))
                .nickname("测试用户")
                .email("test@skygazer.com")
                .isActive(true)
                .build();
            userRepository.save(testUser);
            log.info("Created default test user: test / test123");
        }
        
        log.info("Data initialization completed");
    }
}
