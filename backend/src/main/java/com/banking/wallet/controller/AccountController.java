package com.banking.wallet.controller;

import com.banking.wallet.dto.AccountResponse;
import com.banking.wallet.dto.CreateAccountRequest;
import com.banking.wallet.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }
}