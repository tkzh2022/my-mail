package com.mall.admin.service;

import com.mall.admin.dto.DashboardStatsDTO;
import com.mall.admin.mapper.MerchantMapper;
import com.mall.admin.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatisticsServiceTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private MerchantMapper merchantMapper;

    @InjectMocks
    private AdminStatisticsService adminStatisticsService;

    @Test
    void getDashboardStats_returnsStats() {
        when(productMapper.selectCount(any())).thenReturn(100L, 5L);

        DashboardStatsDTO stats = adminStatisticsService.getDashboardStats();

        assertThat(stats.getTotalProducts()).isEqualTo(100L);
        assertThat(stats.getPendingAudits()).isEqualTo(5L);
        assertThat(stats.getTodayRevenue()).isNotNull();
    }
}
