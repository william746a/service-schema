package com.example.usermanagement.service;

import com.example.usermanagement.domain.UserEntity;
import com.example.usermanagement.dto.UserCreateDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.events.UserCreatedEvent;
import com.example.usermanagement.repo.UserRepository;
import com.example.usermanagement.security.PasswordSecurityService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordSecurityService passwordSecurityService;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository,
                       PasswordSecurityService passwordSecurityService,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordSecurityService = passwordSecurityService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO userDTO) {
        // validation is additionally enforced by annotations; basic checks
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ConflictException("A user with this email already exists.");
        }

        String hashedPassword = passwordSecurityService.hashPassword(userDTO.getPassword());

        UserEntity newUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(userDTO.getEmail())
                .passwordHash(hashedPassword)
                .displayName(userDTO.getDisplayName())
                // createdAt handled by @CreationTimestamp, but set now() as per spec mapping
                .createdAt(OffsetDateTime.now())
                .build();

        UserEntity saved = userRepository.save(newUser);

        // publish event
        eventPublisher.publishEvent(new UserCreatedEvent(this, saved.getId(), saved.getEmail()));

        return UserResponseDTO.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .displayName(saved.getDisplayName())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }
}
