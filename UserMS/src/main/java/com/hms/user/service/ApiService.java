package com.hms.user.service;

import com.hms.user.dto.Roles;
import com.hms.user.dto.UserDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiService {
    WebClient.Builder webClient;

    public Mono<Long> addProfile(UserDTO userDTO) {
        if (userDTO.getRole().equals(Roles.DOCTOR)){
             return webClient.build()
                    .post()
                    .uri("http://localhost:9100/profile/doctor/add")
                    .bodyValue(userDTO)
                    .retrieve()
                    .bodyToMono(Long.class);
        } else if (userDTO.getRole().equals(Roles.PATIENT)){
             return webClient.build()
                    .post()
                    .uri("http://localhost:9100/profile/patient/add")
                    .bodyValue(userDTO)
                    .retrieve()
                    .bodyToMono(Long.class);
        }
        return null;
    }
}
