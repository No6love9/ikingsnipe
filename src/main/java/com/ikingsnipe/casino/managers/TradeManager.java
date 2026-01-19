package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

/**
 * SnipesScripts Enterprise Trade Manager
 * Features:
 * - Two-stage verification (Screen 1 & 2)
 * - Clan Chat notifications for trade safety
 * - Command parsing in trade window
 * - Anti-scam amount change detection
 */
public class TradeManager {
    private final CasinoConfig config;
    private final SessionManager sessionManager;
    private final ProvablyFair provablyFair;
    
    private String currentTrader = null;
    private long verifiedAmount = 0;
    private String selectedGame = "craps";

    public TradeManager(CasinoConfig config, SessionManager sessionManager, ProvablyFair provablyFair) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.provablyFair = provablyFair;
    }

    public void handleTradeScreen1() {
        if (!Trade.isOpen(1)) return;

        String trader = Trade.getTradingWith();
        // Use correct DreamBot API method: getValue(false) for their trade value
        long currentAmount = Trade.getValue(false);

        // Notify Clan Chat on first trade interaction
        if (currentTrader == null || !currentTrader.equals(trader)) {
            currentTrader = trader;
            notifyClanTradeStarted(trader);
        }

        if (currentAmount >= config.minBet) {
            if (Trade.acceptTrade()) {
                verifiedAmount = currentAmount;
                Logger.log("[Trade] Screen 1 Accepted. Amount: " + verifiedAmount);
            }
        }
    }

    public void handleTradeScreen2() {
        if (!Trade.isOpen(2)) return;

        // Use correct DreamBot API method: getValue(false) for their trade value
        long screen2Amount = Trade.getValue(false);

        // CRITICAL: Ensure amount hasn't changed between screens
        if (screen2Amount != verifiedAmount) {
            Logger.warn("[Trade] SCAM DETECTED: Amount changed from " + verifiedAmount + " to " + screen2Amount);
            Trade.declineTrade();
            notifyClanScamAttempt(currentTrader);
            return;
        }

        if (Trade.acceptTrade()) {
            Logger.log("[Trade] Screen 2 Accepted. Bet confirmed: " + verifiedAmount);
            notifyClanTradeSafe(currentTrader, verifiedAmount);
        }
    }

    public void queueTradeRequest(String playerName) {
        Logger.log("[Trade] Queued request from: " + playerName);
    }

    private void notifyClanTradeStarted(String player) {
        if (config.notifyClanOnTrade) {
            String msg = "/[Snipes] Trade started with: " + player + ". Checking bet...";
            Keyboard.type(msg, true);
        }
    }

    private void notifyClanTradeSafe(String player, long amount) {
        if (config.notifyClanOnTrade) {
            String msg = "/[Snipes] " + player + " bet " + formatGP(amount) + ". SAFE TO PROCEED.";
            Keyboard.type(msg, true);
        }
    }

    private void notifyClanScamAttempt(String player) {
        if (config.notifyClanOnTrade) {
            String msg = "/[Snipes] ALERT: " + player + " attempted to change trade amount! DECLINED.";
            Keyboard.type(msg, true);
        }
    }

    private String formatGP(long a) {
        if (a >= 1_000_000) return (a / 1_000_000) + "M";
        if (a >= 1_000) return (a / 1_000) + "K";
        return String.valueOf(a);
    }

    public void reset() {
        currentTrader = null;
        verifiedAmount = 0;
    }
}
