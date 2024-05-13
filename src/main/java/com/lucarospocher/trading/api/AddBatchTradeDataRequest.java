package com.lucarospocher.trading.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddBatchTradeDataRequest(
    @NotNull @NotEmpty List<Float> values
) { }
