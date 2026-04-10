package com.hms.profile.api;

import com.hms.profile.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/profile/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Profile Management", description = "Internal endpoints for retrieving administrator profile information.")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get administrator IDs", description = "Retrieves a list of all internal IDs for profiles with the ADMIN role.")
    @GetMapping("/ids")
    public List<Long> getAdminIds() {
        return adminService.getAdminIds();
    }
}