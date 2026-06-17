package com.mall.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.admin.dto.DashboardStatsDTO;
import com.mall.admin.entity.Product;
import com.mall.admin.mapper.MerchantMapper;
import com.mall.admin.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final ProductMapper productMapper;
    private final MerchantMapper merchantMapper;

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalProducts(productMapper.selectCount(null));
        stats.setPendingAudits(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 0)));
        stats.setTotalUsers(0L);
        stats.setTotalOrders(0L);
        stats.setTodayRevenue(BigDecimal.ZERO);
        return stats;
    }
}
