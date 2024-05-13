package com.lucarospocher.trading.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucarospocher.trading.model.Statistics;
import com.lucarospocher.trading.model.TradeData;
import com.lucarospocher.trading.service.TradeDataService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Integration test on the API level with controller.
 * Database side is mocked, running an actual test version of KDB is out of scope.
 * NOTE: I've not written specific unit tests, given that the application is heavy on IO (DB + http-client) and there
 * is not much logic to be tested so it would have been trivial. In a real application there would be business logic to
 * test and integration + acceptance tests would be many more.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
class TradingSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private TradeDataService mockTradeDataService;

    @Test
    public void test_adding_data_parameters() throws Exception {
        var addRequest = new AddTradeDataRequest(12.12f);
        mockMvc.perform(MockMvcRequestBuilders.post("/add")
                .param("symbol", "VWCE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(addRequest))
            ).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    public void test_no_body_on_add_bad_request() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/add").param("symbol", "VWCE"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void test_fetch_statistics() throws Exception {
        var symbol = "VWCE";
        var k = 2;

        var statsResult = new Statistics(12.12f, 14.14f, 14.14f, 13.13f, 14.01f);

        // Mock the actual service/db call
        Mockito.when(mockTradeDataService.getStats(symbol, k))
                .thenReturn(statsResult);

        mockMvc.perform(MockMvcRequestBuilders.get("/stats")
                .param("symbol", symbol)
                .param("k", String.valueOf(k)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.avg").value(statsResult.avg()));
    }
}