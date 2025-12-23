package com.moiez.pismo.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(
        name = "Transaction Request",
        description = "Represents the create request for a transaction"
)
public record CreateTransactionRequest (

    @Schema(
            description = "Unique account identifier",
            example = "123"
    )
    @NotNull(message = "Account Id is required")
    Long accountId,

    @Schema(
            description = "Type of transaction",
            example = "1",
            allowableValues = {"1", "2", "3", "4"}
    )
    @NotNull(message = "operationTypeId is required")
    Integer operationTypeId,

    @Schema(
            description = "Transaction amount",
            example = "123.45"
    )
    @NotNull(message = "Transaction amount is required")
    BigDecimal amount
) {}