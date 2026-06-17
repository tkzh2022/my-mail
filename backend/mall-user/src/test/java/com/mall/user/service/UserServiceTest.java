package com.mall.user.service;

import com.mall.common.exception.BizException;
import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        User result = userService.createUser("testuser", "test@example.com", "13800138000", "hash");

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPhone()).isEqualTo("13800138000");
        assertThat(result.getStatus()).isEqualTo(1);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void createUser_duplicateUsername_throwsBizException() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> userService.createUser("testuser", "e@e.com", "138", "hash"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40001);
    }

    @Test
    void createUser_duplicatePhone_throwsBizException() {
        when(userMapper.selectCount(any())).thenReturn(0L).thenReturn(1L);

        assertThatThrownBy(() -> userService.createUser("testuser", "e@e.com", "138", "hash"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40002);
    }

    @Test
    void findByAccount_returnsUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userMapper.selectOne(any())).thenReturn(user);

        User result = userService.findByAccount("testuser");
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByAccount_returnsNull() {
        when(userMapper.selectOne(any())).thenReturn(null);
        User result = userService.findByAccount("noone");
        assertThat(result).isNull();
    }

    @Test
    void findById_returnsUser() {
        User user = new User();
        user.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(user);

        User result = userService.findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void updateProfile_success() {
        User user = new User();
        user.setId(1L);
        user.setNickname("old");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        userService.updateProfile(1L, "newNick", "http://avatar.png");

        verify(userMapper).updateById(argThat(u -> "newNick".equals(((User) u).getNickname())));
    }

    @Test
    void updateProfile_userNotFound_throwsBizException() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.updateProfile(99L, "nick", null))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40004);
    }

    @Test
    void updateProfile_nullNickname_doesNotUpdateNickname() {
        User user = new User();
        user.setId(1L);
        user.setNickname("old");
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any())).thenReturn(1);

        userService.updateProfile(1L, null, "http://avatar.png");

        verify(userMapper).updateById(argThat(u -> "old".equals(((User) u).getNickname())));
    }
}
