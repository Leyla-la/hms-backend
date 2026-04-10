package com.hms.user.repository;

import com.hms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.role = com.hms.user.dto.Roles.ADMIN")
    List<Long> findAdminIds();
}
