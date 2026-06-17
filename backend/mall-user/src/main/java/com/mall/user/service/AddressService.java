package com.mall.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BizException;
import com.mall.user.entity.Address;
import com.mall.user.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    public List<Address> listByUserId(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId).orderByDesc(Address::getIsDefault));
    }

    @Transactional
    public Address create(Long userId, Address address) {
        address.setUserId(userId);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }
        addressMapper.insert(address);
        return address;
    }

    @Transactional
    public void update(Long userId, Long addressId, Address updated) {
        Address existing = addressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BizException(40020, "Address not found");
        }
        if (updated.getIsDefault() != null && updated.getIsDefault() == 1) {
            clearDefaultAddress(userId);
        }
        updated.setId(addressId);
        updated.setUserId(userId);
        addressMapper.updateById(updated);
    }

    public void delete(Long userId, Long addressId) {
        Address existing = addressMapper.selectById(addressId);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BizException(40020, "Address not found");
        }
        addressMapper.deleteById(addressId);
    }

    private void clearDefaultAddress(Long userId) {
        List<Address> defaults = addressMapper.selectList(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId).eq(Address::getIsDefault, 1));
        for (Address addr : defaults) {
            addr.setIsDefault(0);
            addressMapper.updateById(addr);
        }
    }
}
