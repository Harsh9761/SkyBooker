package com.example.authService.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    private String provider;

    private boolean isActive = true;

    private String passportNumber;
    private String nationality;

    private LocalDateTime createdAt = LocalDateTime.now();
    
    
    private String otp;
    private LocalDateTime otpExpiry;
    private int otpAttempts;
    private LocalDateTime otpLastSentAt;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getPassportNumber() {
		return passportNumber;
	}

	public void setPassportNumber(String passportNumber) {
		this.passportNumber = passportNumber;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getProvider() {
		return provider;
	}

	public Long getUserId() {
		// TODO Auto-generated method stub
		return userId;
	}
	
	public void setUserId(Long userId) {
	    this.userId = userId;
	}

	public String getPasswordHash() {
		// TODO Auto-generated method stub
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		// TODO Auto-generated method stub
		this.passwordHash = passwordHash;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public LocalDateTime getOtpExpiry() {
		return otpExpiry;
	}

	public void setOtpExpiry(LocalDateTime otpExpiry) {
		this.otpExpiry = otpExpiry;
	}

	public int getOtpAttempts() {
		return otpAttempts;
	}

	public void setOtpAttempts(int otpAttempts) {
		this.otpAttempts = otpAttempts;
	}

	public LocalDateTime getOtpLastSentAt() {
		return otpLastSentAt;
	}

	public void setOtpLastSentAt(LocalDateTime otpLastSentAt) {
		this.otpLastSentAt = otpLastSentAt;
	}

	

    // getters & setters
}
