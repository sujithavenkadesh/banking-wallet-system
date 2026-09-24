package com.banking.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}