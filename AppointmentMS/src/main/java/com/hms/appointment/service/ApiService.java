package com.hms.appointment.service;

import com.hms.appointment.dto.DoctorDTO;
import com.hms.appointment.dto.PatientDTO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiService {
    WebClient.Builder webClient;

    public Mono<Boolean> doctorExists(Long id) {
        return webClient.build()
                .get()
                .uri("http://localhost:9100/profile/doctor/exists/" + id)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> patientExists(Long id) {
        return webClient.build()
                .get()
                .uri("http://localhost:9100/profile/patient/exists/" + id)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<PatientDTO> getPatientById(Long id) {
        return webClient.build()
                .get()
                .uri("http://localhost:9100/profile/patient/get/" + id)
                .retrieve()
                .bodyToMono(PatientDTO.class);
    }

    public Mono<DoctorDTO> getDoctorById(Long id) {
        return webClient.build()
                .get()
                .uri("http://localhost:9100/profile/doctor/get/" + id)
                .retrieve()
                .bodyToMono(DoctorDTO.class);
    }
}
