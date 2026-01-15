package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Sleep;

public class BankingManager {
    private final CasinoConfig config;

    public BankingManager(CasinoConfig config) {
        this.config = config;
    }

    public boolean restock() {
        if (!Bank.isOpen()) {
            if (Bank.open()) {
                Sleep.sleepUntil(Bank::isOpen, 5000);
            }
        }

        if (Bank.isOpen()) {
            // Deposit everything first to have a clean inventory for restocking
            Bank.depositAllItems();
            Sleep.sleep(600, 1200);

            // Withdraw Platinum Tokens first
            if (Bank.contains(CasinoConfig.PLATINUM_TOKEN_ID)) {
                Bank.withdrawAll(CasinoConfig.PLATINUM_TOKEN_ID);
                Sleep.sleepUntil(() -> Inventory.contains(CasinoConfig.PLATINUM_TOKEN_ID), 3000);
            }

            // Withdraw Coins
            if (Bank.contains(CasinoConfig.COINS_ID)) {
                Bank.withdrawAll(CasinoConfig.COINS_ID);
                Sleep.sleepUntil(() -> Inventory.contains(CasinoConfig.COINS_ID), 3000);
            }

            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 3000);
            return true;
        }
        return false;
    }
}
