package com.playtomic.tests.wallet.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.model.Payment;
import com.playtomic.tests.wallet.service.payment.StripeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Acceptance test on the API level with controller, service and database.
 * Payment side is mocked, tested in its own class.
 * NOTE: I've not written specific unit tests, given that the application is heavy on IO (DB + http-client) and there
 * is not much logic to be tested so it would have been trivial. In a real application there would be business logic to
 * test and integration + acceptance tests would be many more.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StripeService mockStripeService;

    @Test
    public void test_wallet_creation_and_fetching() throws Exception {
        var walletCreationMvc = mockMvc.perform(MockMvcRequestBuilders.post("/wallet"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        var walletCreationResponse = walletCreationMvc.getResponse().getContentAsString();
        var walletCreated = mapper.readValue(walletCreationResponse, WalletCreationResponse.class);

        Assertions.assertNotNull(walletCreated.walletId());

        // Get the same wallet that has just been created
        var walletFetchingMvc = mockMvc.perform(
                MockMvcRequestBuilders.get("/wallet").param("walletId", walletCreated.walletId().toString())
            ).andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        var walletFetchingResponse = walletFetchingMvc.getResponse().getContentAsString();
        var walletFetched = mapper.readValue(walletFetchingResponse, Wallet.class);

        Assertions.assertTrue(walletFetched.getBalance().compareTo(BigDecimal.ZERO) == 0);
    }

    @Test
    public void test_wallet_creation_and_fetching_different_one() throws Exception {
        var walletCreationMvc = mockMvc.perform(MockMvcRequestBuilders.post("/wallet"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();
        var walletCreationResponse = walletCreationMvc.getResponse().getContentAsString();
        var walletCreated = mapper.readValue(walletCreationResponse, WalletCreationResponse.class);

        Assertions.assertNotNull(walletCreated.walletId());

        // Get a DIFFERENT wallet, not yet created
        mockMvc.perform(
                MockMvcRequestBuilders.get("/wallet").param("walletId", UUID.randomUUID().toString())
            ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void test_wallet_creation_and_topup() throws Exception {
        var walletCreationMvc = mockMvc.perform(MockMvcRequestBuilders.post("/wallet"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var walletCreationResponse = walletCreationMvc.getResponse().getContentAsString();
        var walletCreated = mapper.readValue(walletCreationResponse, WalletCreationResponse.class);

        Assertions.assertNotNull(walletCreated.walletId());

        // Mock the actual payment call
        Mockito.when(mockStripeService.charge(anyString(), any(BigDecimal.class)))
                .thenReturn(new Payment(UUID.randomUUID().toString()));

        // Charge some money, multiple times
        var topUpRequest = new TopUpRequest("4242 4242 4242 4242", BigDecimal.valueOf(40));
        mockMvc.perform(
            MockMvcRequestBuilders.post("/wallet/top-up")
                .param("walletId", walletCreated.walletId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(topUpRequest))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc.perform(
            MockMvcRequestBuilders.post("/wallet/top-up")
                .param("walletId", walletCreated.walletId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(topUpRequest))
        ).andExpect(MockMvcResultMatchers.status().isOk());

        // Check that wallet balance is correct
        var walletFetchingMvc = mockMvc.perform(
                        MockMvcRequestBuilders.get("/wallet").param("walletId", walletCreated.walletId().toString())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var walletFetchingResponse = walletFetchingMvc.getResponse().getContentAsString();
        var walletFetched = mapper.readValue(walletFetchingResponse, Wallet.class);

        Assertions.assertTrue(walletFetched.getBalance().compareTo(BigDecimal.valueOf(80)) == 0);
    }
}