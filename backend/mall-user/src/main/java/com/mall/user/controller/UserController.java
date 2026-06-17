package com.mall.user.controller;

import com.mall.common.result.R;
import com.mall.user.dto.ProfileUpdateRequest;
import com.mall.user.entity.Address;
import com.mall.user.entity.User;
import com.mall.user.service.AddressService;
import com.mall.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/profile")
    public R<User> getProfile(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(userService.findById(userId));
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestHeader("X-User-Id") Long userId,
                                 @Valid @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(userId, request.getNickname(), request.getAvatarUrl());
        return R.ok();
    }

    @GetMapping("/addresses")
    public R<List<Address>> listAddresses(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(addressService.listByUserId(userId));
    }

    @PostMapping("/addresses")
    public R<Address> createAddress(@RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody Address address) {
        return R.ok(addressService.create(userId, address));
    }

    @PutMapping("/addresses/{id}")
    public R<Void> updateAddress(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable Long id,
                                 @Valid @RequestBody Address address) {
        addressService.update(userId, id, address);
        return R.ok();
    }

    @DeleteMapping("/addresses/{id}")
    public R<Void> deleteAddress(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable Long id) {
        addressService.delete(userId, id);
        return R.ok();
    }
}
