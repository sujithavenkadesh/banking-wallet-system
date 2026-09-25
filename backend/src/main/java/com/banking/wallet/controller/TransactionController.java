package com.banking.wallet.controller;

import com.banking.wallet.dto.TransactionResponse;
import com.banking.wallet.dto.TransferRequest;
import com.banking.wallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> getMyHistory() {
        return ResponseEntity.ok(transactionService.getMyTransactionHistory());
    }

    @GetMapping("/statement/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getStatement(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getStatement(accountNumber));
    }
}