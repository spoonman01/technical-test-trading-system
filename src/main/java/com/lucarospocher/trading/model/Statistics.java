package com.lucarospocher.trading.model;

public record Statistics(
    Float min,
    Float max,
    Float last,
    Float avg,
    Float var
) { }
