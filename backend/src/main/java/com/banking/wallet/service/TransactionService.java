package com.banking.wallet.service;

import com.banking.wallet.dto.TransactionResponse;
import com.banking.wallet.dto.TransferRequest;
import com.banking.wallet.entity.*;
import com.banking.wallet.exception.InsufficientFundsException;
import com.banking.wallet.exception.ResourceNotFoundException;
import com.banking.wallet.repository.AccountRepository;
import com.banking.wallet.repository.AuditLogRepository;
import com.banking.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    // Any transfer at or above this amount gets flagged for review instead of auto-completing
    private static final BigDecimal FRAUD_THRESHOLD = new BigDecimal("50000");

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse transfer(TransferRequest request) {
        String currentUsername = getCurrentUsername();

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Lock both rows in a FIXED order (alphabetical by account number) to prevent deadlocks.
        // If two transfers happen concurrently in opposite directions (A->B and B->A),
        // locking in a consistent order ensures both transactions request locks in the
        // same sequence, avoiding a circular wait (classic deadlock scenario).
        String first = request.getFromAccountNumber().compareTo(request.getToAccountNumber()) < 0
                ? request.getFromAccountNumber() : request.getToAccountNumber();
        String second = request.getFromAccountNumber().compareTo(request.getToAccountNumber()) < 0
                ? request.getToAccountNumber() : request.getFromAccountNumber();

        Account firstLocked = accountRepository.findByAccountNumberForUpdate(first)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + first));
        Account secondLocked = accountRepository.findByAccountNumberForUpdate(second)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + second));

        Account fromAccount = request.getFromAccountNumber().equals(first) ? firstLocked : secondLocked;
        Account toAccount = request.getToAccountNumber().equals(first) ? firstLocked : secondLocked;

        // Ownership check — only the account owner (or admin) can initiate a transfer from it
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !fromAccount.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not own the source account");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            // Log the failed attempt before throwing, for audit trail
            auditLogRepository.save(AuditLog.builder()
                    .action("TRANSFER_FAILED")
                    .performedBy(currentUsername)
                    .details("Insufficient funds: tried to transfer " + request.getAmount()
                            + " from " + fromAccount.getAccountNumber())
                    .build());
            throw new InsufficientFundsException("Insufficient balance in source account");
        }

        boolean isFlagged = request.getAmount().compareTo(FRAUD_THRESHOLD) >= 0;

        Transaction transaction = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(isFlagged ? TransactionStatus.FLAGGED : TransactionStatus.PENDING)
                .remarks(request.getRemarks())
                .build();

        if (isFlagged) {
            // Flagged transactions are recorded but NOT executed automatically —
            // balances are untouched until an admin reviews and approves it.
            transactionRepository.save(transaction);

            auditLogRepository.save(AuditLog.builder()
                    .action("TRANSACTION_FLAGGED")
                    .performedBy(currentUsername)
                    .details("Transfer of " + request.getAmount() + " flagged for review (>= fraud threshold)")
                    .build());

            return toResponse(transaction);
        }

        // Actual balance movement — happens only within this transaction boundary.
        // If anything below throws, @Transactional rolls back both balance changes
        // AND the transaction/audit log inserts, since they're all in one DB transaction.
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);

        auditLogRepository.save(AuditLog.builder()
                .action("FUND_TRANSFER")
                .performedBy(currentUsername)
                .details(String.format("Transferred %s from %s to %s",
                        request.getAmount(), fromAccount.getAccountNumber(), toAccount.getAccountNumber()))
                .build());

        return toResponse(saved);
    }

    public List<TransactionResponse> getStatement(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String currentUsername = getCurrentUsername();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !account.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to view this statement");
        }

        return transactionRepository.findByAccount(account)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getFromAccount() != null ? t.getFromAccount().getAccountNumber() : null,
                t.getToAccount() != null ? t.getToAccount().getAccountNumber() : null,
                t.getAmount(),
                t.getType().name(),
                t.getStatus().name(),
                t.getRemarks(),
                t.getTimestamp()
        );
    }
}