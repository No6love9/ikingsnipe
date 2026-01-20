package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;

public class MuleManager {
    private final CasinoConfig config;
    private boolean mulingInProgress = false;

    public MuleManager(CasinoConfig config) {
        this.config = config;
    }

    public boolean shouldMule() {
        if (!config.autoMule || config.muleName.isEmpty()) return false;
        long currentVal = Inventory.count(CasinoConfig.COINS_ID) + (Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID) * 1000L);
        return currentVal >= config.muleThreshold;
    }

    public boolean handleMuling() {
        mulingInProgress = true;
        Player mule = Players.closest(config.muleName);
        
        if (mule == null) {
            Logger.log("Waiting for mule: " + config.muleName);
            return false;
        }

        if (Trade.isOpen()) {
            if (Trade.isOpen(1)) {
                long currentVal = Inventory.count(CasinoConfig.COINS_ID) + (Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID) * 1000L);
                long toMule = currentVal - config.muleKeepAmount;
                
                if (toMule > 0) {
                    addMuleItems(toMule);
                    Trade.acceptTrade();
                } else {
                    Trade.declineTrade();
                    mulingInProgress = false;
                }
            } else if (Trade.isOpen(2)) {
                if (Trade.acceptTrade()) {
                    Logger.log("Muling successful!");
                    mulingInProgress = false;
                    return true;
                }
            }
        } else {
            mule.interact("Trade with");
            Sleep.sleepUntil(Trade::isOpen, 5000);
        }
        return false;
    }

    private void addMuleItems(long amount) {
        long rem = amount;
        if (rem >= 1000) {
            int tokens = (int)(rem / 1000);
            int invTokens = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
            Trade.addItem(CasinoConfig.PLATINUM_TOKEN_ID, Math.min(tokens, invTokens));
            rem -= (long)Math.min(tokens, invTokens) * 1000L;
        }
        if (rem > 0) {
            int invCoins = Inventory.count(CasinoConfig.COINS_ID);
            Trade.addItem(CasinoConfig.COINS_ID, (int)Math.min(rem, invCoins));
        }
    }

    public boolean isMulingInProgress() {
        return mulingInProgress;
    }

    public boolean performMule() {
        return handleMuling();
    }
}
