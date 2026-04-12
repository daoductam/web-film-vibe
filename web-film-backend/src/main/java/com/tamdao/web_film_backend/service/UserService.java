package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.ChangePasswordRequest;
import com.tamdao.web_film_backend.dto.request.UpdateProfileRequest;
import com.tamdao.web_film_backend.dto.response.UserProfileResponse;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Value("${app.upload.base-url:http://localhost:8081}")
    private String baseUrl;

    /**
     * Get the profile of the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        User user = findUserByUsername(username);
        return toProfileResponse(user);
    }

    /**
     * Update profile (fullName).
     */
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findUserByUsername(username);

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", username);
        return toProfileResponse(user);
    }

    /**
     * Change password — requires current password verification.
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        User user = findUserByUsername(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", username);
    }

    /**
     * Upload avatar image — saves to local filesystem and returns the public URL.
     */
    @Transactional
    public UserProfileResponse uploadAvatar(String username, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Avatar file must not be empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Avatar file size must not exceed 5MB");
        }

        User user = findUserByUsername(username);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(avatarUploadDir);
            Files.createDirectories(uploadPath);

            // Generate unique filename
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = username + "_" + UUID.randomUUID().toString().replace("-", "") + extension;

            // Delete old avatar file if it exists
            if (StringUtils.hasText(user.getAvatarUrl())) {
                deleteOldAvatar(user.getAvatarUrl());
            }

            // Save new file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update avatar URL in DB
            String avatarUrl = baseUrl + "/api/v1/users/avatars/" + fileName;
            user.setAvatarUrl(avatarUrl);
            user = userRepository.save(user);

            log.info("Avatar uploaded for user: {} -> {}", username, fileName);
            return toProfileResponse(user);

        } catch (IOException e) {
            log.error("Failed to save avatar for user: {}", username, e);
            throw new RuntimeException("Failed to save avatar. Please try again.");
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found: " + username));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private void deleteOldAvatar(String avatarUrl) {
        try {
            // Extract filename from URL and delete from disk
            String fileName = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            Path oldFile = Paths.get(avatarUploadDir).resolve(fileName);
            Files.deleteIfExists(oldFile);
        } catch (Exception e) {
            log.warn("Could not delete old avatar file: {}", avatarUrl);
        }
    }
}
