package com.skygazer.weather.service;

import com.skygazer.weather.entity.User;

public interface UserService {
    
    User getUserById(Long id);
    
    User getUserByUsername(String username);
    
    User createUser(User user);
    
    User updateUser(String username, User userUpdate);
    
    User updateDefaultLocation(String username, String location);
    
    void deleteUser(Long id);
}
