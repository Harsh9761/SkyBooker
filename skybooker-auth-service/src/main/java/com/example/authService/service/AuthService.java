package com.example.authService.service;

import com.example.authService.dto.*;
import java.util.*;

public interface AuthService {

    AuthResponse register(RegisterDTO request);

    AuthResponse login(LoginDTO request);

    void logout(String token);

    AuthResponse refreshToken(String token);

    UserDTO getUserById(Long userId);

    UserDTO updateProfile(Long userId, UpdateProfileDTO request);

    void changePassword(Long userId, ChangePasswordDTO request);

    void deactivateAccount(Long userId);

    List<UserDTO> getAllUsers();

    Long validateAndExtractUserId(String token);
}