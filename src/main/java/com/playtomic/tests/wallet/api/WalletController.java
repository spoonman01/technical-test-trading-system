package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.model.Payment;
import com.playtomic.tests.wallet.service.wallet.WalletService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class WalletController {
    private Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // This would be handled differently with proper security, wallets for each user could be added on user creation
    // or on first top-up. For demo purposes I've added this endpoint to create wallets before fetching them or making
    // a top-up.
    @PostMapping("/wallet")
    WalletCreationResponse createWallet() {
        log.info("Creating new wallet");
        var walletId = walletService.addWallet();
        return new WalletCreationResponse(walletId);
    }

    // Returning the entity Wallet as it is pretty simple, usually would map the entity to a relative DTO (WalletResponse)
    @GetMapping("/wallet")
    Wallet getWallet(@RequestParam UUID walletId) {
        log.info("Fetching wallet {}", walletId);
        return walletService.getWallet(walletId);
    }

    // Note that for payments it may be better to use a PUT with some requestPaymentId for idempotency,
    // using POST for simplicity and avoiding more parameters
    @PostMapping("/wallet/top-up")
    Payment topUpWallet(
        @RequestParam UUID walletId,
        @RequestBody @Valid TopUpRequest topUpRequest
    ) {
        log.info("Top-up on wallet {} with card {}", walletId, maskedCreditCard(topUpRequest.creditCardNumber()));
        return walletService.topUpWallet(walletId, topUpRequest);
    }

    /**
     * Return masked version of credit card for logging purposes (e.g. ************4242).
     */
    private String maskedCreditCard(String creditCardNumber) {
        if (creditCardNumber == null || creditCardNumber.isEmpty()) {
            throw new IllegalArgumentException("creditCardNumber cannot be null or empty");
        }

        // Keep the last four digits as they are
        String lastDigits = creditCardNumber.substring(creditCardNumber.length() - 4);

        // Replace all other digits with "*"
        return "*".repeat(creditCardNumber.length() - 4) + lastDigits;
    }
}
