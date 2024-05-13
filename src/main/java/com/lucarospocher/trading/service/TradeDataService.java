package com.lucarospocher.trading.service;

import com.lucarospocher.trading.api.AddBatchTradeDataRequest;
import com.lucarospocher.trading.model.Statistics;
import com.lucarospocher.trading.model.TradeData;
import com.lucarospocher.trading.repository.KdbRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles main logic of trade data management.
 */
@Service
public class TradeDataService {

    private Logger log = LoggerFactory.getLogger(TradeDataService.class);

    private KdbRepository kdbRepository;

    public TradeDataService(KdbRepository kdbRepository) {
        this.kdbRepository = kdbRepository;
    }

    /**
     * Insert a single data point for a given symbol.
     */
    public void addData(TradeData tradeData) {
        kdbRepository.addTradeData(tradeData);
    }

    /**
     * Insert a batch of data points for a given symbol.
     */
    public void addBatchData(String symbol, AddBatchTradeDataRequest addBatchTradeDataRequest) {
        kdbRepository.addBatchTradeData(symbol, addBatchTradeDataRequest.values());
    }

    /**
     * Get statistics for the latest 1e{k} elements with a given symbol or exception if no data is found for that symbol.
     *
     * @throws SymbolNotFoundException if the symbol is missing
     */
    public Statistics getStats(String symbol, Integer k) {
        var stats = kdbRepository.getStatistics(symbol, k);
        if (stats == null || stats.last() == null || stats.last() == 0f) {
            throw new SymbolNotFoundException(symbol);
        }
        return stats;
    }
}
