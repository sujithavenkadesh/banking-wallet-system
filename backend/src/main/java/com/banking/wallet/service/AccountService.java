package com.banking.wallet.service;

import com.banking.wallet.dto.AccountResponse;
import com.banking.wallet.dto.CreateAccountRequest;
import com.banking.wallet.entity.Account;
import com.banking.wallet.entity.AuditLog;
import com.banking.wallet.entity.User;
import com.banking.wallet.exception.ResourceNotFoundException;
import com.banking.wallet.repository.AccountRepository;
import com.banking.wallet.repository.AuditLogRepository;
import com.banking.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String currentUsername = getCurrentUsername();
        User owner = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(request.getInitialBalance())
                .owner(owner)
                .build();

        Account saved = accountRepository.save(account);

        auditLogRepository.save(AuditLog.builder()
                .action("ACCOUNT_CREATED")
                .performedBy(currentUsername)
                .details("Account " + saved.getAccountNumber() + " created with balance " + saved.getBalance())
                .build());

        return toResponse(saved);
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        checkOwnershipOrAdmin(account);
        return toResponse(account);
    }

    public List<AccountResponse> getMyAccounts() {
        String currentUsername = getCurrentUsername();
        User owner = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return accountRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --- helpers ---

    private String generateAccountNumber() {
        // 12-digit numeric account number, prefixed for readability
        StringBuilder sb = new StringBuilder("WAL");
        for (int i = 0; i < 12; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void checkOwnershipOrAdmin(Account account) {
        String currentUsername = getCurrentUsername();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !account.getOwner().getUsername().equals(currentUsername)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to access this account");
        }
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getOwner().getUsername(),
                account.getCreatedAt()
        );
    }
}