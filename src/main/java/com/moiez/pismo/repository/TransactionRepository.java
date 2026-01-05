package com.moiez.pismo.repository;

import com.moiez.pismo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    @Query("select t from Transaction t where t.account.id = :accountId and t.operationType != 'PAYMENT' and t.settled = false")
    List<Transaction> findByAccountId(Long accountId);
}
