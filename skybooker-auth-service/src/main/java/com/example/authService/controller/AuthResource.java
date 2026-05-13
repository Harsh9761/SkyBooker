package com.example.authService.controller;

import com.example.authService.dto.*;
import com.example.authService.entity.Role;
import com.example.authService.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthResource {

    @Autowired
    private AuthService authService;

    //REGISTER
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterDTO request) {
        return authService.register(request);
    }

    // LOGIN
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginDTO request) {
        return authService.login(request);
    }

    // LOGOUT
    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return "Logged out successfully";
    }

    // REFRESH TOKEN
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestHeader("Authorization") String token) {
        return authService.refreshToken(token);
    }

    // 🔹 GET PROFILE
    @GetMapping("/profile")
    public UserDTO getProfile(@RequestHeader("Authorization") String token) {

        token = token.substring(7); // remove Bearer

        Long userId = authService.validateAndExtractUserId(token);

        return authService.getUserById(userId);
    }

    // UPDATE PROFILE
    @PutMapping("/profile")
    public UserDTO updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileDTO request) {

        token = token.substring(7);

        Long userId = authService.validateAndExtractUserId(token);

        return authService.updateProfile(userId, request);
    }

    // CHANGE PASSWORD
    @PutMapping("/password")
    public String changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordDTO request) {

        token = token.substring(7);

        Long userId = authService.validateAndExtractUserId(token);

        authService.changePassword(userId, request);

        return "Password updated successfully";
    }

    // DEACTIVATE ACCOUNT
    @PutMapping("/deactivate")
    public String deactivate(@RequestHeader("Authorization") String token) {

        token = token.substring(7);

        Long userId = authService.validateAndExtractUserId(token);

        authService.deactivateAccount(userId);

        return "Account deactivated";
    }

    // ADMIN: GET ALL USERS
    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return authService.getAllUsers();
    }
    
    
 // SEND OTP
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody ForgotPasswordDTO request) {

        authService.sendOtp(request.getEmail());

        return ResponseEntity.ok(Map.of(
            "message", "OTP sent to email"
        ));
    }

    // VERIFY OTP + RESET PASSWORD
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody ResetPasswordDTO request) {

        authService.verifyOtpAndReset(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(Map.of(
            "message", "Password reset successful"
        ));
    }
    
    @PutMapping("/admin/role")
    public String changeUserRole(
            @RequestParam Long userId,
            @RequestParam Role role) {

        authService.changeUserRole(userId, role);
        return "User role updated successfully";
    }
    
    
    @PutMapping("/admin/activate/{userId}")
    public String adminActivateUser(@PathVariable Long userId) {

        authService.activateAccount(userId);
        return "User activated by admin";
    }
    
    @PostMapping("/send-register-otp")
    public ResponseEntity<String> sendRegisterOtp(
            @RequestBody OtpRequestDTO request) {

        String otp = authService.sendRegisterOtp(request.getEmail());

        return ResponseEntity.ok(
                "OTP sent successfully. Your OTP is: " + otp
        );
    }
    
    @PostMapping("/verify-register-otp")
    public ResponseEntity<Map<String, String>> verifyRegisterOtp(
            @RequestBody OtpRequestDTO request) {

        authService.verifyRegisterOtp(
                request.getEmail(),
                request.getOtp()
        );

        return ResponseEntity.ok(Map.of(
                "message", "OTP verified successfully"
        ));
    }
}