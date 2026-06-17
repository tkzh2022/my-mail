package com.mall.user.controller;

import com.mall.common.result.R;
import com.mall.user.dto.MerchantRegisterRequest;
import com.mall.user.entity.Merchant;
import com.mall.user.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantRegisterController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    public R<Merchant> register(@RequestHeader("X-User-Id") Long userId,
                                @Valid @RequestBody MerchantRegisterRequest request) {
        Merchant merchant = merchantService.register(
                userId, request.getShopName(), request.getBusinessLicense(),
                request.getContactName(), request.getContactPhone(), request.getDescription());
        return R.ok(merchant);
    }
}
