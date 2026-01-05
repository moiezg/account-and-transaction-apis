package com.moiez.pismo.service;

import com.moiez.pismo.api.dto.request.CreateTransactionRequest;
import com.moiez.pismo.api.dto.response.TransactionResponse;
import com.moiez.pismo.constant.ApiConstants;
import com.moiez.pismo.exception.ConflictingRequestException;
import com.moiez.pismo.model.Account;
import com.moiez.pismo.model.OperationType;
import com.moiez.pismo.model.Transaction;
import com.moiez.pismo.repository.TransactionRepository;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.moiez.pismo.constant.ErrorConstants.TRANSACTION_ALREADY_EXISTS;

@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public TransactionResponse createTransaction(
            CreateTransactionRequest request,
            String idempotencyKey
    ) {
        log.info("Processing transaction request for account: {} [{}: {}]", 
                request.accountId(), ApiConstants.IDEMPOTENCY_KEY_HEADER, idempotencyKey);

        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            log.info("Transaction already processed with key: {}", idempotencyKey);
            return mapToTransactionResponse(existingTransaction.get());
        }

        log.debug("Creating new transaction for account: {}", request.accountId());

        BigDecimal finalAmount = request.operationType().isDebit()
                ? request.amount().negate()
                : request.amount();

        if (request.operationType().equals(OperationType.PAYMENT)) {
            finalAmount = settleDischarges(request.accountId(), request.amount());
        }

        Transaction transaction = Transaction.builder()
                .idempotencyKey(idempotencyKey)
                .account(Account.builder().id(request.accountId()).build())
                .operationType(request.operationType())
                .balance(finalAmount)
                .amount(finalAmount)
                .settled(request.operationType().equals(OperationType.PAYMENT))
                .build();

        try {
            Transaction saved = transactionRepository.save(transaction);
            log.info("Transaction created successfully [ID: {}, Account: {}, Amount: {}]", 
                    saved.getId(), saved.getAccount().getId(), saved.getAmount());
            return mapToTransactionResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Transaction conflict detected [Idempotency-Key: {}]", idempotencyKey);
            throw new ConflictingRequestException(TRANSACTION_ALREADY_EXISTS);
        }
    }

    private BigDecimal settleDischarges(Long accountId, BigDecimal paymentAmount) {
        List<Transaction> transactionsToSettle = transactionRepository.findByAccountId(accountId);
        BigDecimal remaining = paymentAmount;

        for (Transaction t : transactionsToSettle) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal currBalance = t.getBalance();

            // only settle negative balances (discharges)
            if (currBalance.compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal amountToApply = remaining.min(currBalance.abs());
                t.setBalance(currBalance.add(amountToApply));
                remaining = remaining.subtract(amountToApply);
                t.setSettled(t.getBalance().compareTo(BigDecimal.ZERO) == 0);
            }
        }

        transactionRepository.saveAll(transactionsToSettle);
        return remaining;
    }


    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .amount(transaction.getAmount())
                .balance(transaction.getBalance())
                .operationType(transaction.getOperationType())
                .eventTimestamp(transaction.getCreatedAt())
                .build();
    }
}
