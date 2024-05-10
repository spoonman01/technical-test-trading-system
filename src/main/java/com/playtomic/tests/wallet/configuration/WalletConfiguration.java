package com.playtomic.tests.wallet.configuration;

import com.playtomic.tests.wallet.service.payment.StripeRestTemplateResponseErrorHandler;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WalletConfiguration {

    @Bean
    public RestTemplate stripeRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
            .errorHandler(new StripeRestTemplateResponseErrorHandler())
            .build();
    }
}
