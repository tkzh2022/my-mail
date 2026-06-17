package com.mall.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsDTO {

    private long totalUsers;
    private long totalOrders;
    private BigDecimal todayRevenue;
    private long totalProducts;
    private long pendingAudits;
}
