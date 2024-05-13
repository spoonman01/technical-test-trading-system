package com.lucarospocher.trading.repository;

import com.lucarospocher.trading.model.Statistics;
import com.lucarospocher.trading.model.TradeData;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class KdbRepository {

    private final JdbcTemplate jdbcTemplate;
    private final static String TRADES_TABLE = "trades";

    public KdbRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addTradeData(TradeData trade) {
        String sql = String.format("q)`%s insert (`%s;%f)", TRADES_TABLE, trade.symbol(), trade.price());
        jdbcTemplate.execute(sql);
    }

    public void addBatchTradeData(String symbol, List<Float> prices) {
        String symbolSqlString = IntStream.range(0, prices.size())
                .mapToObj(idx -> String.format("`%s", symbol))
                .collect(Collectors.joining(""));
        String priceSqlString = IntStream.range(0, prices.size())
                .mapToObj(idx -> String.format("%f", prices.get(idx)))
                .collect(Collectors.joining(" "));

        String sql = String.format("q)`%s insert (%s;%s)", TRADES_TABLE, symbolSqlString, priceSqlString);
        jdbcTemplate.execute(sql);
    }

    public Statistics getStatistics(String symbol, Integer k) {
        String numberOfRecords = new BigDecimal("1e" + k).toPlainString();

        String sqlLast = String.format("q)select [-1] from %s where name=`%s", TRADES_TABLE, symbol);
        var lastElement = jdbcTemplate.queryForRowSet(sqlLast);
        lastElement.next();
        val lastElementPrice = lastElement.getFloat("price");

        String sqlAggregate = String.format("q)select [-%s] min price, max price, avg price, var price from %s where name=`%s",
                numberOfRecords, TRADES_TABLE, symbol);
        var aggregateDataResult = jdbcTemplate.queryForRowSet(sqlAggregate);
        aggregateDataResult.next();
        val minPrice = aggregateDataResult.getFloat(1);
        val maxPrice = aggregateDataResult.getFloat(2);
        val averagePrice = aggregateDataResult.getFloat(3);
        val variancePrice = aggregateDataResult.getFloat(4);

        return new Statistics(minPrice, maxPrice, lastElementPrice, averagePrice, variancePrice);
    }
}
