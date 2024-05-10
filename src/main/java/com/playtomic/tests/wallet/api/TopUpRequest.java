package com.playtomic.tests.wallet.api;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TopUpRequest(
    @NotBlank @Size(min = 16) String creditCardNumber,
    @NotNull @Min(0) @Digits(integer = 18, fraction = 2) BigDecimal amount
) { }
