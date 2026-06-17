package com.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BizException;
import com.mall.user.entity.Merchant;
import com.mall.user.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;

    public Merchant register(Long userId, String shopName, String businessLicense,
                             String contactName, String contactPhone, String description) {
        if (merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId)) > 0) {
            throw new BizException(40010, "User already has a merchant account");
        }
        if (merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getBusinessLicense, businessLicense)) > 0) {
            throw new BizException(40011, "Business license already registered");
        }

        Merchant merchant = new Merchant();
        merchant.setUserId(userId);
        merchant.setShopName(shopName);
        merchant.setBusinessLicense(businessLicense);
        merchant.setContactName(contactName);
        merchant.setContactPhone(contactPhone);
        merchant.setDescription(description);
        merchant.setStatus(0);
        merchantMapper.insert(merchant);
        return merchant;
    }

    public Merchant findByUserId(Long userId) {
        return merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
    }

    public Merchant findById(Long merchantId) {
        return merchantMapper.selectById(merchantId);
    }
}
