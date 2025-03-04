package tw.com.tymbackend.core.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.tymbackend.core.domain.vo.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
} 