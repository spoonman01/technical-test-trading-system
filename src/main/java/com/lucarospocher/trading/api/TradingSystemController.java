package com.lucarospocher.trading.api;

import com.lucarospocher.trading.model.Statistics;
import com.lucarospocher.trading.model.TradeData;
import com.lucarospocher.trading.service.TradeDataService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TradingSystemController {
    private Logger log = LoggerFactory.getLogger(TradingSystemController.class);

    private final TradeDataService tradeDataService;

    public TradingSystemController(TradeDataService tradeDataService) {
        this.tradeDataService = tradeDataService;
    }

    @PostMapping("/add")
    ResponseEntity<?> add(
        @RequestParam String symbol,
        @RequestBody @Valid AddTradeDataRequest addTradeDataRequest
    ) {
        log.info("Adding data for symbol:value {}:{}", symbol, addTradeDataRequest.value());
        tradeDataService.addData(new TradeData(symbol, addTradeDataRequest.value()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/add_batch")
    ResponseEntity<?> addBatch(
        @RequestParam String symbol,
        @RequestBody @Valid AddBatchTradeDataRequest addBatchTradeDataRequest
    ) {
        log.info("Adding batch data for symbol:value {}:{}", symbol, addBatchTradeDataRequest);
        tradeDataService.addBatchData(symbol, addBatchTradeDataRequest);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/stats")
    Statistics getStats(@RequestParam String symbol, @RequestParam Integer k) {
        log.info("Get statistics on last 1e{} records with symbol {}", k, symbol);
        return tradeDataService.getStats(symbol, k);
    }
}
