package com.mall.admin.controller;

import com.mall.admin.dto.DashboardStatsDTO;
import com.mall.admin.service.AdminStatisticsService;
import com.mall.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping("/dashboard")
    public R<DashboardStatsDTO> dashboard() {
        return R.ok(adminStatisticsService.getDashboardStats());
    }
}
