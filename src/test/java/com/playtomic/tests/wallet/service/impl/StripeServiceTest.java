package com.playtomic.tests.wallet.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.model.Payment;
import com.playtomic.tests.wallet.service.payment.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.payment.StripeService;
import com.playtomic.tests.wallet.service.payment.StripeServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * This test is failing with the current implementation.
 *
 * How would you test this?
 *
 * EDIT lucarospocher: The restTemplate inside the service could be mocked and used simply like that,
 * for HTTP related tests I've opted for a more specific MockRestServiceServer (or could use Wiremock which is similar)
 * It allows to test a little bit more, because serialization/deserialization are actually performed.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class StripeServiceTest {

    @Autowired
    private StripeService stripeService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;
    @Value("${stripe.simulator.charges-uri}") URI chargesUri;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void test_exception() throws StripeServiceException, JsonProcessingException {
        var creditCardNumber = "4242 4242 4242 4242";
        var amount = new BigDecimal(5);
        var charge = new StripeService.ChargeRequest(creditCardNumber, amount);
        mockServer.expect(ExpectedCount.once(), requestTo(chargesUri))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(mapper.writeValueAsString(charge)))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        Assertions.assertThrows(StripeAmountTooSmallException.class, () -> {
            stripeService.charge(creditCardNumber, amount);
        });
    }

    @Test
    public void test_ok() throws StripeServiceException, JsonProcessingException {
        var creditCardNumber = "4242 4242 4242 4242";
        var amount = new BigDecimal(15);
        var charge = new StripeService.ChargeRequest(creditCardNumber, amount);
        var payment = new Payment("pid_123456");
        mockServer.expect(ExpectedCount.once(), requestTo(chargesUri))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().string(mapper.writeValueAsString(charge)))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(payment))
            );

        var paymentResult = stripeService.charge(creditCardNumber, amount);
        mockServer.verify();
        Assertions.assertEquals(paymentResult, payment);
    }
}
