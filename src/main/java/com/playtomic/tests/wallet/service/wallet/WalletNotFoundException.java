package com.playtomic.tests.wallet.service.wallet;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {
    private final UUID walletId;
    public WalletNotFoundException(UUID walletId) {
        this.walletId = walletId;
    }

    public UUID getWalletId() {
        return walletId;
    }
}
