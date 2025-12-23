package com.moiez.pismo.service;

import com.moiez.pismo.api.dto.request.CreateTransactionRequest;
import com.moiez.pismo.api.dto.response.AccountResponse;
import com.moiez.pismo.api.dto.response.TransactionResponse;
import com.moiez.pismo.exception.BadRequestException;
import com.moiez.pismo.model.OperationType;
import com.moiez.pismo.model.Transaction;
import com.moiez.pismo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUnitTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    private static final Long ACCOUNT_ID = 1L;
    // ----------------------------------------------------
    // Debit transaction
    // ----------------------------------------------------

    @Test
    void createTransaction_shouldNegateAmount_whenOperationIsDebit() {
        // given
        BigDecimal amount = new BigDecimal("100.00");

        CreateTransactionRequest request =
                new CreateTransactionRequest(ACCOUNT_ID, OperationType.WITHDRAWAL.getId(), amount);

        when(accountService.getAccount(ACCOUNT_ID))
                .thenReturn(AccountResponse.builder()
                        .id(ACCOUNT_ID)
                        .documentNumber("12345678900")
                        .build());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TransactionResponse response = transactionService.createTransaction(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo("-100.00");
        assertThat(response.operationType()).isEqualTo(OperationType.WITHDRAWAL);
        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);

        verify(accountService).getAccount(ACCOUNT_ID);
        verify(transactionRepository).save(any(Transaction.class));
        verifyNoMoreInteractions(accountService, transactionRepository);
    }

    // ----------------------------------------------------
    // Credit transaction
    // ----------------------------------------------------

    @Test
    void createTransaction_shouldKeepAmountPositive_whenOperationIsCredit() {
        // given
        BigDecimal amount = new BigDecimal("50.00");

        CreateTransactionRequest request =
                new CreateTransactionRequest(ACCOUNT_ID, OperationType.PAYMENT.getId(), amount);

        when(accountService.getAccount(ACCOUNT_ID))
                .thenReturn(AccountResponse.builder()
                        .id(ACCOUNT_ID)
                        .documentNumber("12345678900")
                        .build());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TransactionResponse response = transactionService.createTransaction(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo("50.00");
        assertThat(response.operationType()).isEqualTo(OperationType.PAYMENT);
        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID);

        verify(accountService).getAccount(ACCOUNT_ID);
        verify(transactionRepository).save(any(Transaction.class));
        verifyNoMoreInteractions(accountService, transactionRepository);
    }

    // ----------------------------------------------------
    // Invalid operation type
    // ----------------------------------------------------

    @Test
    void createTransaction_shouldThrowException_whenOperationTypeIsInvalid() {
        // given
        CreateTransactionRequest request =
                new CreateTransactionRequest(ACCOUNT_ID, 999, new BigDecimal("10.00"));

        when(accountService.getAccount(ACCOUNT_ID))
                .thenReturn(AccountResponse.builder()
                        .id(ACCOUNT_ID)
                        .documentNumber("12345678900")
                        .build());

        // when / then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid operation type");

        verify(accountService).getAccount(ACCOUNT_ID);
        verifyNoInteractions(transactionRepository);
    }
}
