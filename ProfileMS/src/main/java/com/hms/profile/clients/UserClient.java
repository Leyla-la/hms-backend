package com.hms.profile.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import com.hms.profile.config.FeignSecurityInterceptor;

@FeignClient(name = "UserMS", configuration = FeignSecurityInterceptor.class)
public interface UserClient {

    @GetMapping("/users/admin/ids")
    List<Long> getAdminIds();
}