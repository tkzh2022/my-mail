package com.mall.auth.feign;

import com.mall.auth.feign.dto.CreateUserRequest;
import com.mall.auth.feign.dto.UserDTO;
import com.mall.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mall-user")
public interface UserFeignClient {

    @PostMapping("/internal/users")
    R<UserDTO> createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/internal/users/by-account")
    R<UserDTO> findByAccount(@RequestParam("account") String account);
}
