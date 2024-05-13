package com.lucarospocher.trading.api;

import jakarta.validation.constraints.*;

public record AddTradeDataRequest(
    @NotNull Float value
) { }
