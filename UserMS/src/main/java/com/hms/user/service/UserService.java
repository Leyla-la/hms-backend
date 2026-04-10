package com.hms.user.service;

import com.hms.user.dto.UserDTO;
import com.hms.user.exception.HmsException;
import java.util.List;

public interface UserService {
    void registerUser(UserDTO userDTO) throws HmsException;
    UserDTO loginUser(UserDTO userDTO) throws HmsException;
    UserDTO getUserById(Long id) throws HmsException;
    void updateUser(UserDTO userDTO) throws HmsException;
    UserDTO getUser(String email) throws HmsException;
    List<Long> getAdminIds() throws HmsException;
    long count();
    Long getProfile(Long id) throws HmsException;
}
