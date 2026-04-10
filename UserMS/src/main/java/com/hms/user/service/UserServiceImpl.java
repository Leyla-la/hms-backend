package com.hms.user.service;

import com.hms.user.clients.ProfileClient;
import com.hms.user.dto.Roles;
import com.hms.user.dto.UserDTO;
import com.hms.user.entity.User;
import com.hms.user.exception.HmsException;
import com.hms.user.repository.UserRepository;
import com.hms.user.notification.NotificationPublisher;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service("userService")
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final ApiService apiService;
    final ProfileClient profileClient;
    final NotificationPublisher notificationPublisher;

    @Override
    public void registerUser(UserDTO userDTO) throws HmsException {
        Optional<User> opt = userRepository.findByEmail(userDTO.getEmail());
        if (opt.isPresent()) {
            throw new HmsException("USER_ALREADY_EXISTS");
        }
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        Long profileId = null;
        if (userDTO.getRole().equals(Roles.DOCTOR)) {
            profileId = profileClient.addDoctor(userDTO);
        } else if (userDTO.getRole().equals(Roles.PATIENT)) {
            profileId = profileClient.addPatient(userDTO);
        }
        userDTO.setProfileId(profileId);
        User savedUser = userRepository.save(userDTO.toUser());

        // Publish notification
        notificationPublisher.publish(
                "hms.user.registered",
                savedUser.getId().toString(),
                savedUser.toUserDTO(),
                "USER_REGISTERED",
                "UserMS",
                userRepository.findAdminIds()
        );
    }

    @Override
    public UserDTO loginUser(UserDTO userDTO) throws HmsException {
        User user = userRepository.findByEmail(userDTO.getEmail()).orElseThrow(() -> new HmsException("USER_NOT_FOUND"));
        if(!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new HmsException("INVALID_CREDENTIALS");
        }
        user.setPassword(null);
        return user.toUserDTO();
    }

    @Override
    public UserDTO getUserById(Long id) throws HmsException {
        return userRepository.findById(id).orElseThrow(() -> new HmsException("USER_NOT_FOUND")).toUserDTO();
    }

    @Override
    public void updateUser(UserDTO userDTO) {

    }

    @Override
    public UserDTO getUser(String email) throws HmsException {
        return userRepository.findByEmail(email).orElseThrow(() -> new HmsException("USER_NOT_FOUND")).toUserDTO();
    }

    @Override
    public List<Long> getAdminIds() throws HmsException {
        return userRepository.findAdminIds();
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public Long getProfile(Long id) throws HmsException {
        User user = userRepository.findById(id).orElseThrow(() -> new HmsException("USER_NOT_FOUND"));
        return user.getProfileId();
    }
}
