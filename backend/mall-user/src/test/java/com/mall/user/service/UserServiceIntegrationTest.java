package com.mall.user.service;

import com.mall.common.exception.BizException;
import com.mall.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_andFindByAccount_fullFlow() {
        User created = userService.createUser("integration_user", "int@test.com", "18900000001", "hash123");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo("integration_user");
        assertThat(created.getStatus()).isEqualTo(1);

        User found = userService.findByAccount("integration_user");
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());

        User byEmail = userService.findByAccount("int@test.com");
        assertThat(byEmail).isNotNull();

        User byPhone = userService.findByAccount("18900000001");
        assertThat(byPhone).isNotNull();
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        userService.createUser("dup_user", "first@test.com", "18900000010", "hash");

        assertThatThrownBy(() -> userService.createUser("dup_user", "second@test.com", "18900000011", "hash"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40001);
    }

    @Test
    void createUser_duplicatePhone_throwsException() {
        userService.createUser("user_a", "a@test.com", "18900000020", "hash");

        assertThatThrownBy(() -> userService.createUser("user_b", "b@test.com", "18900000020", "hash"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40002);
    }

    @Test
    void updateProfile_updatesNicknameAndAvatar() {
        User user = userService.createUser("profile_user", "p@test.com", "18900000030", "hash");

        userService.updateProfile(user.getId(), "NewNick", "http://avatar.png");

        User updated = userService.findById(user.getId());
        assertThat(updated.getNickname()).isEqualTo("NewNick");
        assertThat(updated.getAvatarUrl()).isEqualTo("http://avatar.png");
    }

    @Test
    void findById_nonExistent_returnsNull() {
        User result = userService.findById(99999L);
        assertThat(result).isNull();
    }
}
