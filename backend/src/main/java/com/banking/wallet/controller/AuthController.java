package com.banking.wallet.controller;

import com.banking.wallet.dto.AuthResponse;
import com.banking.wallet.dto.LoginRequest;
import com.banking.wallet.dto.RegisterRequest;
import com.banking.wallet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/set-pin")
    public ResponseEntity<Void> setPin(@jakarta.validation.Valid @RequestBody com.banking.wallet.dto.SetPinRequest request) {
    authService.setTransactionPin(request.getPin());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/has-pin")
    public ResponseEntity<Boolean> hasPin() {
        return ResponseEntity.ok(authService.hasPinSet());
    }
}