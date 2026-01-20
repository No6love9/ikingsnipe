package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.database.DatabaseManager;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Logger;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
            DatabaseManager.updateBalance(currentTrader, verifiedValueGP);
            DatabaseManager.recordGame(currentTrader, "DEPOSIT", verifiedValueGP, "SUCCESS");
            notifyClanTradeSafe(currentTrader, verifiedValueGP);
        }
    }

    public void queueTradeRequest(String playerName) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Logger.log("[Trade] " + playerName + " is trading @ " + timestamp);
        
        // Automated trade acceptance message
        if (config.autoAcceptTrades) {
            Keyboard.type("/" + playerName + " is trading", true);
        }
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
        return BalanceManager.formatGP(a);
    }

    public void reset() {
        currentTrader = null;
        verifiedValueGP = 0;
    }

    public long getVerifiedValueGP() {
        return verifiedValueGP;
    }

    /**
     * Sends payout to player via trade
     */
    public static void sendPayout(String player, long amount) {
        try {
            // Queue payout for manual processing or automated trade
            Logger.log(String.format("[Payout] Queued %s GP for %s", BalanceManager.formatGP(amount), player));
            // In a real implementation, this would initiate a trade with the player
            // For now, we log it and the admin can manually process
        } catch (Exception e) {
            Logger.error("[Payout] Error sending payout to " + player + ": " + e.getMessage());
        }
    }
}
