package com.skygazer.weather.service.impl;

import com.skygazer.weather.entity.User;
import com.skygazer.weather.exception.BusinessException;
import com.skygazer.weather.repository.UserRepository;
import com.skygazer.weather.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        return userRepository.save(user);
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
                userRepository.existsByEmail(userUpdate.getEmail())) {
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
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateDefaultLocation(String username, String location) {
        User user = getUserByUsername(username);
        user.setDefaultLocation(location);
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        userRepository.deleteById(id);
    }
}
