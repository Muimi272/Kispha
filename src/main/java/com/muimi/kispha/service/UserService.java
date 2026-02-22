package com.muimi.kispha.service;


import com.muimi.kispha.entity.User;

import java.util.List;

public interface UserService {
    public List<User> findAll();

    public User findByUid(Long uid);

    public List<User> findByUsernameContaining(String username);

    public User register(User user);

    public User updateUser(User user);

    public User login(User user);

    public boolean deleteByUid(Long uid, User admin);
}
