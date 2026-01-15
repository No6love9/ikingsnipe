package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

public class BankingManager {
    private final CasinoConfig config;
    private int failCount = 0;

    public BankingManager(CasinoConfig config) {
        this.config = config;
    }

    public boolean restock() {
        if (!Bank.isOpen()) {
            Logger.log("Opening bank...");
            if (Bank.open()) {
                Sleep.sleepUntil(Bank::isOpen, 5000);
            }
            return false;
        }

        long currentGP = Inventory.count(CasinoConfig.COINS_ID);
        long currentTokens = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
        long totalValue = currentGP + (currentTokens * 1000L);

        // If we already have enough, just close and finish
        if (totalValue >= config.restockAmount) {
            Logger.log("Already have enough stock. Closing bank.");
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 3000);
            failCount = 0;
            return true;
        }

        // Deposit everything first to have a clean slate
        Logger.log("Depositing all items for a clean restock...");
        Bank.depositAllItems();
        Sleep.sleepUntil(Inventory::isEmpty, 3000);

        // Withdraw GP
        if (Bank.count(CasinoConfig.COINS_ID) > 0) {
            long toWithdraw = Math.min(Bank.count(CasinoConfig.COINS_ID), config.restockAmount);
            Logger.log("Withdrawing GP: " + toWithdraw);
            Bank.withdraw(CasinoConfig.COINS_ID, (int)toWithdraw);
            Sleep.sleepUntil(() -> Inventory.count(CasinoConfig.COINS_ID) > 0, 3000);
        }

        // Verify again
        currentGP = Inventory.count(CasinoConfig.COINS_ID);
        currentTokens = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
        totalValue = currentGP + (currentTokens * 1000L);

        if (totalValue >= config.restockThreshold || Bank.count(CasinoConfig.COINS_ID) == 0) {
            Logger.log("Restock successful or bank empty. Closing.");
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 3000);
            failCount = 0;
            return true;
        }

        failCount++;
        if (failCount > 3) {
            Logger.log("Banking failed 3 times. Forcing close to prevent loop.");
            Bank.close();
            failCount = 0;
            return true;
        }

        return false;
    }
}
