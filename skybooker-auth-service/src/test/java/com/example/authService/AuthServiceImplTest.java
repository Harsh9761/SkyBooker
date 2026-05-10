package com.example.authService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.example.authService.dto.*;
import com.example.authService.entity.*;
import com.example.authService.repository.UserRepository;
import com.example.authService.service.AuthServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthServiceImpl authService;

    // ---------------- REGISTER SUCCESS ----------------
    @Test
    void testRegister() {

        RegisterDTO dto = new RegisterDTO();
        dto.setFullName("John");
        dto.setEmail("john@test.com");
        dto.setPassword("1234");
        dto.setPhone("9999999999");
        dto.setNationality("Indian");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setUserId(1L);
            u.setRole(Role.PASSENGER);
            return u;
        });

        AuthResponse response = authService.register(dto);

        assertNotNull(response);
        assertEquals("PASSENGER", response.getRole());
    }

    // ---------------- REGISTER EMAIL EXISTS ----------------
    @Test
    void testRegister_EmailAlreadyExists() {

        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("john@test.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(dto));

        assertEquals("Email already exists", ex.getMessage());
    }

    // ---------------- LOGIN SUCCESS ----------------
    @Test
    void testLogin() {

        LoginDTO dto = new LoginDTO();
        dto.setEmail("john@test.com");
        dto.setPassword("1234");

        User user = new User();
        user.setUserId(1L);
        user.setEmail("john@test.com");
        user.setPasswordHash(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .encode("1234")
        );
        user.setActive(true);
        user.setRole(Role.PASSENGER);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        AuthResponse response = authService.login(dto);

        assertNotNull(response);
        assertEquals("PASSENGER", response.getRole());
    }

    // ---------------- LOGIN WRONG PASSWORD ----------------
    @Test
    void testLogin_InvalidPassword() {

        LoginDTO dto = new LoginDTO();
        dto.setEmail("john@test.com");
        dto.setPassword("wrong");

        User user = new User();
        user.setEmail("john@test.com");
        user.setPasswordHash(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                        .encode("correct")
        );
        user.setActive(true);

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(dto));

        assertEquals("Invalid credentials", ex.getMessage());
    }

    // ---------------- LOGIN USER NOT FOUND ----------------
    @Test
    void testLogin_UserNotFound() {

        LoginDTO dto = new LoginDTO();
        dto.setEmail("notfound@test.com");

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(dto));

        assertEquals("User not found", ex.getMessage());
    }

    // ---------------- GET USER SUCCESS ----------------
    @Test
    void testGetUserById() {

        User user = new User();
        user.setUserId(1L);
        user.setFullName("John");
        user.setEmail("john@test.com");
        user.setRole(Role.PASSENGER);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserDTO result = authService.getUserById(1L);

        assertNotNull(result);
        assertEquals("John", result.getFullName());
    }

    // ---------------- GET USER NOT FOUND ----------------
    @Test
    void testGetUserById_NotFound() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.getUserById(99L));

        assertEquals("User not found", ex.getMessage());
    }


    // ---------------- DEACTIVATE ACCOUNT ----------------
    @Test
    void testDeactivateAccount() {

        User user = new User();
        user.setUserId(1L);
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        authService.deactivateAccount(1L);

        assertFalse(user.isActive());
    }

    // ---------------- DEACTIVATE ALREADY INACTIVE ----------------
    @Test
    void testDeactivateAccount_AlreadyInactive() {

        User user = new User();
        user.setUserId(1L);
        user.setActive(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.deactivateAccount(1L));

        assertEquals("User is already deactivated", ex.getMessage());
    }
}