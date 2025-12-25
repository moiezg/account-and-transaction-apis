package com.moiez.pismo.service;

import com.moiez.pismo.api.dto.request.CreateAccountRequest;
import com.moiez.pismo.api.dto.response.AccountResponse;
import com.moiez.pismo.exception.BadRequestException;
import com.moiez.pismo.exception.ConflictingRequestException;
import com.moiez.pismo.exception.NotFoundException;
import com.moiez.pismo.model.Account;
import com.moiez.pismo.repository.AccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .map(this::mapToAccountResponse)
                .orElseGet(() -> {
                    try {
                        Account account = new Account();
                        account.setDocumentNumber(request.documentNumber());
                        account.setIdempotencyKey(idempotencyKey);
                        return mapToAccountResponse(repository.save(account));
                    } catch (DataIntegrityViolationException e) {
                        throw new ConflictingRequestException("Account already exists");
                    }
                });
    }

    public AccountResponse getAccount(Long id) {
        return repository.findById(id)
                .map(this::mapToAccountResponse)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    public void applyTransaction(Long accountId, BigDecimal amount) {
        Account account = repository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BadRequestException("Invalid account"));

        BigDecimal updatedBalance = amount.add(account.getBalance());
        if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Insufficient funds");
        }

        account.setBalance(updatedBalance);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .documentNumber(account.getDocumentNumber())
                .balance(account.getBalance())
                .build();
    }
}