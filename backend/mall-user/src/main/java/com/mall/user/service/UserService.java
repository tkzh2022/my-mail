package com.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BizException;
import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User createUser(String username, String email, String phone, String passwordHash) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            throw new BizException(40001, "Username already exists");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0) {
            throw new BizException(40002, "Phone already registered");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordHash);
        user.setNickname(username);
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }

    public User findByAccount(String account) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, account)
                .or().eq(User::getEmail, account)
                .or().eq(User::getPhone, account));
    }

    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    public void updateProfile(Long userId, String nickname, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(40004, "User not found");
        }
        if (nickname != null) user.setNickname(nickname);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
    }
}
