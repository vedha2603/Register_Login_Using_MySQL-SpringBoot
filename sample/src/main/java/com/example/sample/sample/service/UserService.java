package com.example.sample.sample.service;

import com.example.sample.sample.model.User;

public interface UserService {
    void saveUser(User user);
    User findByEmail(String email);
}
