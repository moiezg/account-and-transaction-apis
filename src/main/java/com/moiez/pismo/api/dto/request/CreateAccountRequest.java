package com.moiez.pismo.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "Create Account Request",
        description = "Represents the create request for an account"
)
public record CreateAccountRequest(
        @Schema(description = "Unique Identifier of the account number",
                example = "12345")
        @NotNull(message = "Document number is required")
        String documentNumber)
{}
