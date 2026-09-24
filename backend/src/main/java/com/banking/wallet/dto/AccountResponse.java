package com.banking.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String ownerUsername;
    private LocalDateTime createdAt;
}