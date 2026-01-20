package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

public class BankingManager {
    private final CasinoConfig config;
    private int failCount = 0;
    private String currentStatus = "Idle";

    public BankingManager(CasinoConfig config) {
        this.config = config;
    }

    public String getStatus() {
        return currentStatus;
    }

    public boolean shouldBank() {
        long currentGP = Inventory.count(CasinoConfig.COINS_ID);
        long currentTokens = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
        long totalValue = currentGP + (currentTokens * 1000L);
        return totalValue < config.restockThreshold;
    }

    public boolean restock() {
        if (!Bank.isOpen()) {
            currentStatus = "Opening Bank...";
            Logger.log(currentStatus);
            
            // Aggressive opening: Try NPC first, then GameObject
            NPC banker = NPCs.closest(n -> n != null && n.hasAction("Bank"));
            if (banker != null) {
                banker.interact("Bank");
            } else {
                GameObject booth = GameObjects.closest(g -> g != null && g.hasAction("Bank"));
                if (booth != null) {
                    booth.interact("Bank");
                }
            }
            
            Sleep.sleepUntil(Bank::isOpen, 5000);
            if (!Bank.isOpen()) {
                failCount++;
                if (failCount > 5) {
                    currentStatus = "Bank Open Failed - Retrying...";
                    return false;
                }
            }
            return false;
        }

        currentStatus = "Bank Open - Verifying Inventory";
        long currentGP = Inventory.count(CasinoConfig.COINS_ID);
        long currentTokens = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
        long totalValue = currentGP + (currentTokens * 1000L);

        if (totalValue >= config.restockAmount) {
            currentStatus = "Stock Sufficient - Closing";
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 3000);
            failCount = 0;
            return true;
        }

        currentStatus = "Depositing Items...";
        Bank.depositAllItems();
        Sleep.sleepUntil(Inventory::isEmpty, 3000);

        currentStatus = "Withdrawing GP...";
        if (Bank.count(CasinoConfig.COINS_ID) > 0) {
            long toWithdraw = Math.min(Bank.count(CasinoConfig.COINS_ID), config.restockAmount);
            Bank.withdraw(CasinoConfig.COINS_ID, (int)toWithdraw);
            Sleep.sleepUntil(() -> Inventory.count(CasinoConfig.COINS_ID) > 0, 3000);
        } else {
            currentStatus = "ERROR: No GP in Bank!";
            Logger.log(currentStatus);
            Bank.close();
            return true; // Exit loop even if empty to prevent stuck
        }

        currentStatus = "Final Verification...";
        currentGP = Inventory.count(CasinoConfig.COINS_ID);
        totalValue = currentGP + (Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID) * 1000L);

        if (totalValue >= config.restockThreshold || Bank.count(CasinoConfig.COINS_ID) == 0) {
            currentStatus = "Restock Complete";
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 3000);
            failCount = 0;
            return true;
        }

        return false;
    }
}
