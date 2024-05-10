package com.playtomic.tests.wallet.service.wallet;

import com.playtomic.tests.wallet.api.TopUpRequest;
import com.playtomic.tests.wallet.entity.Charge;
import com.playtomic.tests.wallet.entity.Wallet;
import com.playtomic.tests.wallet.model.Payment;
import com.playtomic.tests.wallet.repository.ChargeRepository;
import com.playtomic.tests.wallet.repository.WalletRepository;
import com.playtomic.tests.wallet.service.payment.StripeService;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Handles main logic of wallet management and charging money in it.
 */
@Service
public class WalletService {

    private Logger log = LoggerFactory.getLogger(WalletService.class);
    private final static int MONEY_SCALE = 2; // Assume a simple currency with 2 decimals

    private StripeService stripeService;
    private WalletRepository walletRepository;
    private ChargeRepository chargeRepository;

    public WalletService(StripeService stripeService, WalletRepository walletRepository, ChargeRepository chargeRepository) {
        this.stripeService = stripeService;
        this.walletRepository = walletRepository;
        this.chargeRepository = chargeRepository;
    }

    /**
     * Upsert wallet and return its id.
     */
    public UUID addWallet() {
        Wallet newWallet = new Wallet();
        newWallet.setBalance(BigDecimal.ZERO);
        var wallet = walletRepository.save(newWallet);
        return wallet.getId();
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(@NonNull UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    /**
     * Add credit to a given wallet by making a payment on Stripe with a given credit card.
     * <p>
     * Once performed, the payment will be stored in the database and the balance of the wallet increased accordingly.
     * Note that a pessimistic lock is put on the wallet record during these operations in case any of them failed.
     * Actual exception rollback the transaction and are handled upwards on the API level.
     * Any money/currency operation is simplified, payments require knowing the currency being used and either perform
     * conversions or take care of different "precision" (not all currencies have max 2 decimals). Also the rounding is
     * a complex issue and it was also simplified
     * </p>
     */
    @Transactional
    public Payment topUpWallet(@NonNull UUID walletId, @NonNull TopUpRequest topUpRequest) {
        var wallet = walletRepository.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));
        var payment = stripeService.charge(topUpRequest.creditCardNumber(), topUpRequest.amount());
        log.info("Performed payment {}, top-up {} on wallet {}", payment, topUpRequest.amount(), wallet);

        var charge = new Charge();
        charge.setAmount(topUpRequest.amount().setScale(MONEY_SCALE, RoundingMode.HALF_DOWN));
        charge.setPaymentId(payment.getId());
        charge.setWalletId(wallet.getId());
        chargeRepository.save(charge);

        wallet.setBalance(wallet.getBalance().add(topUpRequest.amount()).setScale(MONEY_SCALE, RoundingMode.HALF_DOWN));
        walletRepository.save(wallet);

        return payment;
    }
}
