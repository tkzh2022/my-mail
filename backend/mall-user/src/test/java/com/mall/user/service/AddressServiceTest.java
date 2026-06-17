package com.mall.user.service;

import com.mall.common.exception.BizException;
import com.mall.user.entity.Address;
import com.mall.user.mapper.AddressMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    @Test
    void listByUserId_returnsAddresses() {
        Address addr = new Address();
        addr.setId(1L);
        addr.setUserId(1L);
        when(addressMapper.selectList(any())).thenReturn(List.of(addr));

        List<Address> result = addressService.listByUserId(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void create_nonDefaultAddress_success() {
        Address address = new Address();
        address.setIsDefault(0);
        when(addressMapper.insert(any())).thenReturn(1);

        Address result = addressService.create(1L, address);
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(addressMapper).insert(any());
    }

    @Test
    void create_defaultAddress_clearsExistingDefaults() {
        Address address = new Address();
        address.setIsDefault(1);

        Address existingDefault = new Address();
        existingDefault.setId(5L);
        existingDefault.setUserId(1L);
        existingDefault.setIsDefault(1);
        when(addressMapper.selectList(any())).thenReturn(List.of(existingDefault));
        when(addressMapper.updateById(any())).thenReturn(1);
        when(addressMapper.insert(any())).thenReturn(1);

        addressService.create(1L, address);

        verify(addressMapper).updateById(argThat(a -> ((Address) a).getIsDefault() == 0));
        verify(addressMapper).insert(any());
    }

    @Test
    void update_success() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(1L);
        when(addressMapper.selectById(1L)).thenReturn(existing);
        when(addressMapper.updateById(any())).thenReturn(1);

        Address updated = new Address();
        updated.setIsDefault(0);
        addressService.update(1L, 1L, updated);

        verify(addressMapper).updateById(any());
    }

    @Test
    void update_addressNotFound_throwsBizException() {
        when(addressMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> addressService.update(1L, 99L, new Address()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40020);
    }

    @Test
    void update_wrongUser_throwsBizException() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(2L);
        when(addressMapper.selectById(1L)).thenReturn(existing);

        assertThatThrownBy(() -> addressService.update(1L, 1L, new Address()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40020);
    }

    @Test
    void delete_success() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(1L);
        when(addressMapper.selectById(1L)).thenReturn(existing);
        when(addressMapper.deleteById(1L)).thenReturn(1);

        addressService.delete(1L, 1L);
        verify(addressMapper).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsBizException() {
        when(addressMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> addressService.delete(1L, 99L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40020);
    }
}
