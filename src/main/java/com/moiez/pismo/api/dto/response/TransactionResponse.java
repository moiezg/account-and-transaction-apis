package com.moiez.pismo.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moiez.pismo.model.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Schema
@Builder
public record TransactionResponse(

        @Schema(
                description = "Unique identifier of the transaction",
                example = "1"
        )
        Long transactionId,

        @Schema(
                description = "Unique identifier of the account to which the transaction belongs",
                example = "1"
        )
        Long  accountId,

        @Schema(
                description = "Transaction amount",
                example = "150.75",
                multipleOf = 0.01
        )
        @Digits(integer = 10, fraction = 2)
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal amount,

        @Digits(integer = 10, fraction = 2)
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal balance,

        @Schema(
                description = "Transaction operation type",
                example = "PAYMENT"
        )
        OperationType operationType,

        @Schema(
                description = "Transaction creation timestamp",
                example = "2024-06-01T12:30:45",
                format = "date-time"
        )
        @NotNull
        Instant eventTimestamp
) {
}
