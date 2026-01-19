package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.utils.ProvablyFair;
import com.ikingsnipe.database.DatabaseManager;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Logger;

/**
 * SnipesScripts Enterprise Trade Manager v9.0
 * Features:
 * - Dual Currency Support (Gold Coins & Platinum Tokens)
 * - Database Balance Integration
 * - Anti-Scam Verification
 */
public class TradeManager {
    private final CasinoConfig config;
    private final DatabaseManager dbManager;
    
    private String currentTrader = null;
    private long verifiedValueGP = 0;

    public TradeManager(CasinoConfig config, DatabaseManager dbManager) {
        this.config = config;
        this.dbManager = dbManager;
    }

    public void handleTradeScreen1() {
        if (!Trade.isOpen(1)) return;

        String trader = Trade.getTradingWith();
        long totalValueGP = calculateTradeValueGP();

        if (currentTrader == null || !currentTrader.equals(trader)) {
            currentTrader = trader;
            notifyClanTradeStarted(trader);
        }

        if (totalValueGP >= config.minBet) {
            if (Trade.acceptTrade()) {
                verifiedValueGP = totalValueGP;
                Logger.log("[Trade] Screen 1 Accepted. Total GP Value: " + verifiedValueGP);
            }
        }
    }

    public void handleTradeScreen2() {
        if (!Trade.isOpen(2)) return;

        long screen2ValueGP = calculateTradeValueGP();

        if (screen2ValueGP != verifiedValueGP) {
            Logger.warn("[Trade] SCAM DETECTED: Value changed from " + verifiedValueGP + " to " + screen2ValueGP);
            Trade.declineTrade();
            notifyClanScamAttempt(currentTrader);
            return;
        }

        if (Trade.acceptTrade()) {
            Logger.log("[Trade] Screen 2 Accepted. Deposit confirmed: " + verifiedValueGP);
            dbManager.recordGame(currentTrader, "deposit", verifiedValueGP, 0, "DEPOSIT", "");
            notifyClanTradeSafe(currentTrader, verifiedValueGP);
        }
    }

    public void queueTradeRequest(String playerName) {
        Logger.log("[Trade] Queued request from: " + playerName);
    }

    private long calculateTradeValueGP() {
        long total = 0;
        Item[] items = Trade.getTheirItems();
        if (items == null) return 0;

        for (Item item : items) {
            if (item == null) continue;
            if (item.getID() == CasinoConfig.COINS_ID) {
                total += item.getAmount();
            } else if (item.getID() == CasinoConfig.PLATINUM_TOKEN_ID) {
                total += (long) item.getAmount() * 1000L;
            }
        }
        return total;
    }

    private void notifyClanTradeStarted(String player) {
        if (config.notifyClanOnTrade) {
            Keyboard.type("/[Snipes] Trade with " + player + ". Checking Coins/Tokens...", true);
        }
    }

    private void notifyClanTradeSafe(String player, long amount) {
        if (config.notifyClanOnTrade) {
            Keyboard.type("/[Snipes] " + player + " deposited " + formatGP(amount) + ". Balance Updated.", true);
        }
    }

    private void notifyClanScamAttempt(String player) {
        if (config.notifyClanOnTrade) {
            Keyboard.type("/[Snipes] ALERT: " + player + " attempted to swap items! DECLINED.", true);
        }
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }

    public void reset() {
        currentTrader = null;
        verifiedValueGP = 0;
    }

    public long getVerifiedValueGP() {
        return verifiedValueGP;
    }
}
