package com.moiez.pismo.api.dto.response;

import lombok.Builder;


@Builder
public record AccountResponse(
        Long id,
        String documentNumber
) {}