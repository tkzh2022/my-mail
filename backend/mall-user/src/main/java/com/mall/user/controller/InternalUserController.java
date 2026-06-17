package com.mall.user.controller;

import com.mall.common.result.R;
import com.mall.user.dto.CreateUserRequest;
import com.mall.user.entity.User;
import com.mall.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @PostMapping
    public R<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getUsername(), request.getEmail(),
                request.getPhone(), request.getPasswordHash());
        return R.ok(user);
    }

    @GetMapping("/by-account")
    public R<User> findByAccount(@RequestParam String account) {
        return R.ok(userService.findByAccount(account));
    }
}
