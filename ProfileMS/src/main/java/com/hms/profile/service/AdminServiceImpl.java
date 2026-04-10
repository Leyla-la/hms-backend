package com.hms.profile.service;

import com.hms.profile.clients.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserClient userClient;

    @Override
    public List<Long> getAdminIds() {
        return userClient.getAdminIds();
    }
}