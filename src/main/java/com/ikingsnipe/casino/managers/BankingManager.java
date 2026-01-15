package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Sleep;

public class BankingManager {
    private final CasinoConfig config;
    public BankingManager(CasinoConfig config) { this.config = config; }
    public void restock() {
        if (Bank.open()) {
            Sleep.sleepUntil(Bank::isOpen, 5000);
            if (Bank.contains(CasinoConfig.PLATINUM_TOKEN_ID)) Bank.withdrawAll(CasinoConfig.PLATINUM_TOKEN_ID);
            if (Bank.contains(CasinoConfig.COINS_ID)) Bank.withdrawAll(CasinoConfig.COINS_ID);
            Bank.close();
        }
    }
}
