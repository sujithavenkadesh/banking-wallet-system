package com.banking.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank(message = "Source account number is required")
    private String fromAccountNumber;

    @jakarta.validation.constraints.NotBlank(message = "Transaction PIN is required")
    private String pin;

    @NotBlank(message = "Destination account number is required")
    private String toAccountNumber;

    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private BigDecimal amount;

    private String remarks;
}