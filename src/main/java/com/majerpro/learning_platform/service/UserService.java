package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.UserLoginDto;
import com.majerpro.learning_platform.dto.UserRegistrationDto;
import com.majerpro.learning_platform.dto.UserResponseDto;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setFullName(dto.getFullName());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public UserResponseDto loginUser(UserLoginDto dto) {
        User user = userRepository.findByUsername(dto.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(dto.getUsernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify hashed password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return convertToDto(user);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    private UserResponseDto convertToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
}
