package com.hms.notification.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "UserMS", path = "/users")
public interface UserClient {

    @GetMapping("/getProfile/{id}")
    Long getProfile(@PathVariable("id") Long id);

    @GetMapping("/admin/ids")
    List<Long> getAdminIds();

    @GetMapping("/ids/role/{roleName}")
    List<Long> getUserIdsByRole(@PathVariable("roleName") String roleName);
}
