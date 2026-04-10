package com.hms.appointment.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "user-ms")
public interface UserClient {

    @GetMapping("/users/ids/role/ADMIN")
    List<Long> getAdminIds();
}
