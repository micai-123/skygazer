package com.skygazer.weather.service.impl;

import com.skygazer.weather.entity.User;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.mapper.UserMapper;
import com.skygazer.weather.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    
    @Override
    public User getUserById(Long id) {
        return userMapper.findById(id)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        if (userMapper.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (user.getEmail() != null && userMapper.existsByEmail(user.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        userMapper.save(user);
        return user;
    }
    
    @Override
    @Transactional
    public User updateUser(String username, User userUpdate) {
        User user = getUserByUsername(username);
        
        if (userUpdate.getNickname() != null) {
            user.setNickname(userUpdate.getNickname());
        }
        if (userUpdate.getAvatar() != null) {
            user.setAvatar(userUpdate.getAvatar());
        }
        if (userUpdate.getEmail() != null) {
            if (!user.getEmail().equals(userUpdate.getEmail()) && 
                userMapper.existsByEmail(userUpdate.getEmail())) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPhone() != null) {
            user.setPhone(userUpdate.getPhone());
        }
        if (userUpdate.getPreferredTheme() != null) {
            user.setPreferredTheme(userUpdate.getPreferredTheme());
        }
        
        userMapper.save(user);
        return user;
    }
    
    @Override
    @Transactional
    public User updateDefaultLocation(String username, String location) {
        User user = getUserByUsername(username);
        user.setDefaultLocation(location);
        userMapper.save(user);
        return user;
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userMapper.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
    }
}
