package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.UserLoginDto;
import com.majerpro.learning_platform.dto.UserRegistrationDto;
import com.majerpro.learning_platform.dto.UserResponseDto;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponseDto registerUser(UserRegistrationDto dto) {
        // Check if username already exists
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // TODO: Hash password in Stage 9 (Security)
        user.setFullName(dto.getFullName());
        user.setIsActive(true);

        // Save to database
        User savedUser = userRepository.save(user);

        // Convert to response DTO
        return convertToDto(savedUser);
    }

    public UserResponseDto loginUser(UserLoginDto dto) {
        // Find user by username or email
        User user = userRepository.findByUsername(dto.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(dto.getUsernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check password (TODO: Hash comparison in Stage 9)
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Update last login
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
