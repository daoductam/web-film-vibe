package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.ChangePasswordRequest;
import com.tamdao.web_film_backend.dto.request.UpdateProfileRequest;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.UserProfileResponse;
import com.tamdao.web_film_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Manage the authenticated user's profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns the current user's profile information")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getProfile(userDetails.getUsername()),
                "Profile fetched successfully"
        ));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update fullName")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(userDetails.getUsername(), request),
                "Profile updated successfully"
        ));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change my password", description = "Change password — requires current password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload my avatar", description = "Upload a profile picture (max 5MB, image only)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.uploadAvatar(userDetails.getUsername(), file),
                "Avatar uploaded successfully"
        ));
    }

    /**
     * Serve avatar files stored on the VPS filesystem.
     * Accessible at: GET /api/v1/users/avatars/{filename}
     */
    @GetMapping("/avatars/{filename:.+}")
    @Operation(summary = "Get avatar file", description = "Serve the stored avatar image file")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/avatars").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
