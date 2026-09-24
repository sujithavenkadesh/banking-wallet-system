package com.banking.wallet.service;

import com.banking.wallet.dto.AuthResponse;
import com.banking.wallet.dto.LoginRequest;
import com.banking.wallet.dto.RegisterRequest;
import com.banking.wallet.entity.AuditLog;
import com.banking.wallet.entity.Role;
import com.banking.wallet.entity.User;
import com.banking.wallet.exception.DuplicateResourceException;
import com.banking.wallet.repository.AuditLogRepository;
import com.banking.wallet.repository.UserRepository;
import com.banking.wallet.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .action("USER_REGISTERED")
                .performedBy(user.getUsername())
                .details("New user registered with role " + user.getRole())
                .build());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails, user.getRole().name());

        auditLogRepository.save(AuditLog.builder()
                .action("USER_LOGIN")
                .performedBy(user.getUsername())
                .details("User logged in")
                .build());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}