package com.mall.common.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class SentinelConfig {

    /*
     * Nacos dynamic rule data source (application.yml):
     *
     * spring.cloud.sentinel.datasource.flow.nacos.server-addr=${spring.cloud.nacos.server-addr}
     * spring.cloud.sentinel.datasource.flow.nacos.dataId=${spring.application.name}-flow-rules
     * spring.cloud.sentinel.datasource.flow.nacos.groupId=SENTINEL_GROUP
     * spring.cloud.sentinel.datasource.flow.nacos.rule-type=flow
     *
     * spring.cloud.sentinel.datasource.degrade.nacos.server-addr=${spring.cloud.nacos.server-addr}
     * spring.cloud.sentinel.datasource.degrade.nacos.dataId=${spring.application.name}-degrade-rules
     * spring.cloud.sentinel.datasource.degrade.nacos.groupId=SENTINEL_GROUP
     * spring.cloud.sentinel.datasource.degrade.nacos.rule-type=degrade
     */

    @Bean
    public BlockExceptionHandler blockExceptionHandler(ObjectMapper objectMapper) {
        return (HttpServletRequest request, HttpServletResponse response, BlockException e) -> {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(R.fail(429, "Too many requests")));
        };
    }
}
