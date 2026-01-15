package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.CasinoController;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.games.GameResult;
import com.ikingsnipe.casino.utils.DreamBotAdapter;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.wrappers.items.Item;

import java.util.*;

/**
 * Manages the complete trade flow from initiation to payout
 * Implements the 8-step mandatory trade process
 */
public class TradeManager {
    
    private static final int COINS_ID = 995;
    
    private final CasinoController controller;
    private final SessionManager sessionManager;
    private final GameManager gameManager;
    private final DreamBotAdapter apiAdapter;
    
    private final Map<String, TradeState> tradeStates = new HashMap<>();
    private final Map<String, Long> tradeTimers = new HashMap<>();
    
    private enum TradeState {
        INITIALIZED,
        OFFER_RECEIVED,
        OFFER_VALIDATED,
        TRADE_ACCEPTED,
        GAME_LOCKED,
        GAME_RESOLVED,
        PAYOUT_PENDING,
        PAYOUT_SENT,
        COMPLETED,
        FAILED
    }
    
    // Statistics
    private int totalTrades = 0;
    private int successfulTrades = 0;
    private int failedTrades = 0;
    private long totalProfit = 0;
    
    public TradeManager(CasinoController controller, SessionManager sessionManager, 
                       GameManager gameManager, DreamBotAdapter apiAdapter) {
        this.controller = controller;
        this.sessionManager = sessionManager;
        this.gameManager = gameManager;
        this.apiAdapter = apiAdapter;
    }
    
    /**
     * Main trade processing method
     */
    public void process() {
        if (!apiAdapter.isTradeOpen()) {
            return;
        }
        
        String tradingWith = apiAdapter.getTradePartner();
        if (tradingWith == null) {
            apiAdapter.declineTrade();
            return;
        }
        
        try {
            // Get or create trade state
            TradeState state = tradeStates.getOrDefault(tradingWith, TradeState.INITIALIZED);
            
            // Process based on state
            switch (state) {
                case INITIALIZED:
                    handleTradeInitiation(tradingWith);
                    break;
                case OFFER_RECEIVED:
                    handleOfferValidation(tradingWith);
                    break;
                case OFFER_VALIDATED:
                    handleTradeAcceptance(tradingWith);
                    break;
                case TRADE_ACCEPTED:
                    handleGameLock(tradingWith);
                    break;
                case GAME_LOCKED:
                    handleGameResolution(tradingWith);
                    break;
                case GAME_RESOLVED:
                    handlePayoutPreparation(tradingWith);
                    break;
                case PAYOUT_PENDING:
                    handlePayoutExecution(tradingWith);
                    break;
            }
            
            // Check for trade timeout
            checkTradeTimeout(tradingWith);
            
        } catch (Exception e) {
            controller.logError("Trade processing error for " + tradingWith + ": " + e.getMessage());
            failTrade(tradingWith, "System error");
        }
    }
    
    /**
     * Step 1-2: Trade initiation and session creation
     */
    private void handleTradeInitiation(String playerName) {
        if (!apiAdapter.isFirstTradeScreen()) {
            return;
        }
        
        controller.log("Trade initiated with " + playerName);
        
        // Check if player has an existing session
        PlayerSession session = sessionManager.getSession(playerName);
        if (session != null) {
            // Resume existing session
            tradeStates.put(playerName, TradeState.OFFER_RECEIVED);
            tradeTimers.put(playerName, System.currentTimeMillis());
            controller.log("Resuming existing session for " + playerName);
        } else {
            // New trade without session (will be created from chat command)
            controller.log("New trade from " + playerName + " (awaiting !bet command)");
        }
        
        tradeStates.put(playerName, TradeState.OFFER_RECEIVED);
        tradeTimers.put(playerName, System.currentTimeMillis());
    }
    
    /**
     * Step 3: Validate trade offer
     */
    private void handleOfferValidation(String playerName) {
        if (!apiAdapter.isFirstTradeScreen()) {
            return;
        }
        
        PlayerSession session = sessionManager.getSession(playerName);
        if (session == null) {
            // No session, decline trade
            controller.log("No session for " + playerName + ", declining trade");
            apiAdapter.declineTrade();
            failTrade(playerName, "No active session");
            return;
        }
        
        // Get offered coins
        int offeredCoins = getOfferedCoins();
        
        // Validate amount matches session bet
        if (offeredCoins != session.getBetAmount()) {
            controller.log("Bet mismatch for " + playerName + 
                ": Expected " + session.getBetAmount() + ", got " + offeredCoins);
            
            apiAdapter.sendPrivateMessage(playerName, 
                "Bet mismatch! Expected " + session.getBetAmount() + " GP, got " + offeredCoins + " GP");
            apiAdapter.declineTrade();
            failTrade(playerName, "Bet amount mismatch");
            return;
        }
        
        // Validate game is still enabled
        if (!gameManager.isGameEnabled(session.getGameType())) {
            controller.log("Game disabled for " + playerName + ": " + session.getGameType());
            apiAdapter.sendPrivateMessage(playerName, 
                session.getGameType() + " is currently disabled. Please choose another game.");
            apiAdapter.declineTrade();
            failTrade(playerName, "Game disabled");
            return;
        }
        
        // Offer validated successfully
        controller.log("Trade offer validated for " + playerName + ": " + offeredCoins + " GP");
        tradeStates.put(playerName, TradeState.OFFER_VALIDATED);
        
        // Auto-accept if enabled
        if (controller.getConfig().isAutoAcceptTrades()) {
            apiAdapter.acceptTrade();
        }
    }
    
    /**
     * Step 4-5: Accept trade and lock game
     */
    private void handleTradeAcceptance(String playerName) {
        if (!apiAdapter.isSecondTradeScreen()) {
            return;
        }
        
        // Accept the trade
        apiAdapter.acceptTrade();
        
        // Wait for trade to complete
        if (!apiAdapter.waitForTradeComplete(3000)) {
            controller.log("Trade acceptance timeout for " + playerName);
            return;
        }
        
        controller.log("Trade completed with " + playerName);
        tradeStates.put(playerName, TradeState.TRADE_ACCEPTED);
        totalTrades++;
        
        // Lock the game selection
        handleGameLock(playerName);
    }
    
    /**
     * Step 6: Lock game selection
     */
    private void handleGameLock(String playerName) {
        PlayerSession session = sessionManager.getSession(playerName);
        if (session == null) {
            failTrade(playerName, "Session lost after trade");
            return;
        }
        
        // Mark game as locked
        session.setGameLocked(true);
        sessionManager.updateSession(session);
        
        controller.log("Game locked for " + playerName + ": " + session.getGameType());
        tradeStates.put(playerName, TradeState.GAME_LOCKED);
        
        // Move to game resolution
        handleGameResolution(playerName);
    }
    
    /**
     * Step 7: Resolve game
     */
    private void handleGameResolution(String playerName) {
        PlayerSession session = sessionManager.getSession(playerName);
        if (session == null) {
            failTrade(playerName, "Session lost during game resolution");
            return;
        }
        
        // Play the game
        GameResult result = gameManager.playGame(session.getGameType(), 
            session.getBetAmount(), playerName);
        
        // Update session with result
        session.setGameResult(result);
        session.setWin(result.isWin());
        session.setPayout(result.getPayout());
        session.setGameComplete(true);
        sessionManager.updateSession(session);
        
        // Send result message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("payout", String.valueOf(result.getPayout()));
        placeholders.put("player", playerName);
        
        if (result.isWin()) {
            String winMessage = controller.getConfig().getWinMessage(
                session.getGameType(), placeholders);
            apiAdapter.sendPrivateMessage(playerName, winMessage);
        } else {
            String lossMessage = controller.getConfig().getLossMessage(
                session.getGameType(), placeholders);
            apiAdapter.sendPrivateMessage(playerName, lossMessage);
        }
        
        // Also send game result
        apiAdapter.sendPrivateMessage(playerName, result.getMessage());
        
        controller.log("Game resolved for " + playerName + ": " + 
            (result.isWin() ? "WIN" : "LOSS") + " (" + result.getPayout() + " GP)");
        
        tradeStates.put(playerName, TradeState.GAME_RESOLVED);
        
        if (result.isWin()) {
            // Move to payout
            handlePayoutPreparation(playerName);
        } else {
            // House wins, complete trade
            completeTrade(playerName, false);
        }
    }
    
    /**
     * Step 8: Prepare and execute payout
     */
    private void handlePayoutPreparation(String playerName) {
        PlayerSession session = sessionManager.getSession(playerName);
        if (session == null || !session.isWin()) {
            failTrade(playerName, "Invalid payout state");
            return;
        }
        
        // Check if we have enough coins for payout
        if (!hasSufficientCoins(session.getPayout())) {
            controller.logError("Insufficient coins for payout to " + playerName + 
                ": Need " + session.getPayout() + " GP");
            
            // Try to withdraw from bank
            if (!withdrawFromBank(session.getPayout())) {
                failTrade(playerName, "Insufficient funds for payout");
                return;
            }
        }
        
        // Open trade for payout
        if (apiAdapter.openTrade(playerName)) {
            controller.log("Opening payout trade for " + playerName);
            tradeStates.put(playerName, TradeState.PAYOUT_PENDING);
            tradeTimers.put(playerName, System.currentTimeMillis());
        } else {
            failTrade(playerName, "Failed to open payout trade");
        }
    }
    
    private void handlePayoutExecution(String playerName) {
        if (!apiAdapter.isTradeOpen()) {
            return;
        }
        
        PlayerSession session = sessionManager.getSession(playerName);
        if (session == null) {
            failTrade(playerName, "Session lost during payout");
            return;
        }
        
        // Add coins to trade
        if (apiAdapter.isFirstTradeScreen()) {
            if (apiAdapter.addItemToTrade(COINS_ID, session.getPayout())) {
                controller.log("Added " + session.getPayout() + " GP to payout for " + playerName);
                
                // Accept trade
                if (apiAdapter.isSecondTradeScreen()) {
                    apiAdapter.acceptTrade();
                    
                    // Wait for completion
                    if (apiAdapter.waitForTradeComplete(3000)) {
                        controller.log("Payout completed for " + playerName);
                        completeTrade(playerName, true);
                        
                        // Update profit (negative for payout)
                        totalProfit -= session.getPayout();
                    }
                }
            }
        }
    }
    
    /**
     * Process payout trades specifically
     */
    public void processPayout() {
        // Find players in PAYOUT_PENDING state
        for (Map.Entry<String, TradeState> entry : tradeStates.entrySet()) {
            if (entry.getValue() == TradeState.PAYOUT_PENDING) {
                handlePayoutExecution(entry.getKey());
            }
        }
    }
    
    /**
     * Complete a trade successfully
     */
    private void completeTrade(String playerName, boolean payoutSuccess) {
        PlayerSession session = sessionManager.getSession(playerName);
        
        if (payoutSuccess && session != null && session.isWin()) {
            successfulTrades++;
            controller.log("Trade completed successfully with " + playerName + 
                " (payout: " + session.getPayout() + " GP)");
        } else if (session != null && !session.isWin()) {
            // House win
            successfulTrades++;
            totalProfit += session.getBetAmount();
            controller.log("House wins from " + playerName + 
                " (profit: +" + session.getBetAmount() + " GP)");
        }
        
        // Clean up
        tradeStates.remove(playerName);
        tradeTimers.remove(playerName);
        
        if (session != null) {
            sessionManager.removeSession(playerName);
        }
    }
    
    /**
     * Fail a trade with reason
     */
    private void failTrade(String playerName, String reason) {
        controller.logError("Trade failed with " + playerName + ": " + reason);
        
        // Send failure message if possible
        apiAdapter.sendPrivateMessage(playerName, 
            "Trade failed: " + reason + ". Please try again.");
        
        // Decline any open trade
        if (apiAdapter.isTradeOpen()) {
            apiAdapter.declineTrade();
        }
        
        // Clean up
        tradeStates.remove(playerName);
        tradeTimers.remove(playerName);
        sessionManager.removeSession(playerName);
        
        failedTrades++;
    }
    
    /**
     * Check for trade timeouts
     */
    private void checkTradeTimeout(String playerName) {
        Long startTime = tradeTimers.get(playerName);
        if (startTime == null) return;
        
        long timeout = controller.getConfig().getTradeTimeoutSeconds() * 1000L;
        if (System.currentTimeMillis() - startTime > timeout) {
            controller.log("Trade timeout for " + playerName);
            failTrade(playerName, "Trade timeout");
        }
    }
    
    /**
     * Emergency stop all trades
     */
    public void emergencyStop() {
        controller.log("Emergency stopping all trades...");
        
        // Decline all open trades
        if (apiAdapter.isTradeOpen()) {
            apiAdapter.declineTrade();
        }
        
        // Fail all pending trades
        for (String playerName : new ArrayList<>(tradeStates.keySet())) {
            failTrade(playerName, "Emergency stop");
        }
        
        // Clear all
        tradeStates.clear();
        tradeTimers.clear();
    }
    
    /**
     * Check if player has an active trade
     */
    public boolean hasActiveTrade(String playerName) {
        return tradeStates.containsKey(playerName) && 
               tradeStates.get(playerName) != TradeState.COMPLETED && 
               tradeStates.get(playerName) != TradeState.FAILED;
    }
    
    // === UTILITY METHODS ===
    
    private int getOfferedCoins() {
        return apiAdapter.getOfferedItemAmount(COINS_ID);
    }
    
    private boolean hasSufficientCoins(int amount) {
        return apiAdapter.getItemCount(COINS_ID) >= amount;
    }
    
    private boolean withdrawFromBank(int amount) {
        if (!controller.getConfig().isEnableBankWithdrawal()) {
            return false;
        }
        
        controller.log("Attempting bank withdrawal of " + amount + " GP");
        return apiAdapter.withdrawFromBank(COINS_ID, amount, controller.getConfig().getBankPin());
    }
    
    // === STATISTICS ===
    
    public Map<String, Object> getTradeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrades", totalTrades);
        stats.put("successfulTrades", successfulTrades);
        stats.put("failedTrades", failedTrades);
        stats.put("totalProfit", totalProfit);
        stats.put("activeTrades", tradeStates.size());
        return stats;
    }
    
    public void shutdown() {
        emergencyStop();
        controller.log("TradeManager shut down");
    }
}