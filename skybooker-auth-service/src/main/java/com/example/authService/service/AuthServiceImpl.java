package com.example.authService.service;

import com.example.authService.dto.*;
import com.example.authService.entity.Role;
import com.example.authService.entity.User;
import com.example.authService.repository.UserRepository;
import com.example.authService.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private JwtUtil jwtUtil = new JwtUtil();
    
    @Autowired
    private EmailService emailService;

    // REGISTER
    @Override
    public AuthResponse register(RegisterDTO request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(encoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setPassportNumber(request.getPassportNumber());
        user.setNationality(request.getNationality());
        user.setRole(Role.PASSENGER);
        user.setProvider("LOCAL");

        userRepository.save(user);

        String token = jwtUtil.generateToken(
        	    user.getEmail(),
        	    user.getRole().name(),
        	    user.getUserId().intValue()
        	);

        return new AuthResponse(token, user.getRole().name(), user.getUserId());
    }

    // LOGIN
    @Override
    public AuthResponse login(LoginDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        String token = jwtUtil.generateToken(
        	    user.getEmail(),
        	    user.getRole().name(),
        	    user.getUserId().intValue()
        	);

        return new AuthResponse(token, user.getRole().name(), user.getUserId());
    }

    // LOGOUT (JWT = stateless)
    @Override
    public void logout(String token) {
        // For now do nothing (frontend will discard token)
        // Advanced: token blacklist (Redis)
    }

    // REFRESH TOKEN
    @Override
    public AuthResponse refreshToken(String token) {

        token = token.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }

        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtUtil.generateToken(
        	    user.getEmail(),
        	    user.getRole().name(),
        	    user.getUserId().intValue()
        	);

        return new AuthResponse(newToken, user.getRole().name(), user.getUserId());
    }

    // GET USER BY ID
    @Override
    public UserDTO getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDTO(user);
    }

    // UPDATE PROFILE
    @Override
    public UserDTO updateProfile(Long userId, UpdateProfileDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setNationality(request.getNationality());

        userRepository.save(user);

        return mapToDTO(user);
    }

    // CHANGE PASSWORD
    @Override
    public void changePassword(Long userId, ChangePasswordDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPasswordHash(encoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    //  DEACTIVATE ACCOUNT
    @Override
    public void deactivateAccount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);

        userRepository.save(user);
    }

    //  GET ALL USERS
    @Override
    public List<UserDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    //  VALIDATE + EXTRACT USER ID FROM TOKEN
    @Override
    public Long validateAndExtractUserId(String token) {

        token = token.replace("Bearer ", "");

        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }

        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getUserId();
    }

    //MAPPER METHOD
    private UserDTO mapToDTO(User user) {

        UserDTO dto = new UserDTO();

        dto.setUserId(user.getUserId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setPassportNumber(user.getPassportNumber());
        dto.setNationality(user.getNationality());

        return dto;
    }
    
    
    @Override
    public void sendOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpLastSentAt() != null &&
            user.getOtpLastSentAt().plusSeconds(30).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Wait before requesting OTP");
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        user.setOtp(encoder.encode(otp));
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpAttempts(0);
        user.setOtpLastSentAt(LocalDateTime.now());

        userRepository.save(user);

        emailService.send(email, "OTP for Reset Password", "OTP: " + otp);
    }
    
    
    @Override
    public void verifyOtpAndReset(String email, String otp, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpExpiry() == null ||
            user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (user.getOtpAttempts() >= 5) {
            throw new RuntimeException("Too many attempts");
        }

        if (!encoder.matches(otp, user.getOtp())) {
            user.setOtpAttempts(user.getOtpAttempts() + 1);
            userRepository.save(user);
            throw new RuntimeException("Invalid OTP");
        }

        user.setPasswordHash(encoder.encode(newPassword));

        user.setOtp(null);
        user.setOtpExpiry(null);
        user.setOtpAttempts(0);

        userRepository.save(user);
    }
}