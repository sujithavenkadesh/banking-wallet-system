package com.banking.wallet.repository;

import com.banking.wallet.entity.Account;
import com.banking.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // For account statements — all transactions where account was sender or receiver
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.owner.username = :username " +
       "OR t.toAccount.owner.username = :username ORDER BY t.timestamp DESC")
    List<Transaction> findAllByUsername(@Param("username") String username);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.timestamp DESC")
    List<Transaction> findByAccount(@Param("account") Account account);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) " +
           "AND t.timestamp BETWEEN :start AND :end ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountAndDateRange(
        @Param("account") Account account,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    List<Transaction> findByStatus(com.banking.wallet.entity.TransactionStatus status);
}