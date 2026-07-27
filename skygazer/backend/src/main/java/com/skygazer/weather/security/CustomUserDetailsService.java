package com.skygazer.weather.security;

import com.skygazer.weather.entity.User;
import com.skygazer.weather.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户明细服务：根据用户名从数据库加载用户信息并适配为 Spring Security 的 {@link UserDetails}。
 *
 * <p>当前所有注册用户统一授予 {@code ROLE_USER} 角色；账号是否可用取决于 {@code User#isActive} 字段。
 * 该服务被 {@link JwtAuthenticationFilter} 在令牌校验阶段调用，以确认用户真实存在且处于激活状态。</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserMapper userMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getIsActive(),
            true,
            true,
            true,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
