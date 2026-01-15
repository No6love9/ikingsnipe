package com.ikingsnipe.casino;

import com.ikingsnipe.casino.managers.GameManager;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.utils.DreamBotAdapter;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Sleep;

import java.awt.*;

@ScriptManifest(
    name = "Elite Titan Casino v9.0",
    description = "Proactive casino host with interactive trade messaging and robust game logic.",
    author = "ikingsnipe",
    version = 9.0,
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript {

    private CasinoConfig config;
    private DreamBotAdapter adapter;
    private GameManager gameManager;
    
    private int wins = 0;
    private int losses = 0;
    private long startTime;
    private String lastPlayer = "None";
    private boolean sentSafetyMsg = false;

    @Override
    public void onStart() {
        log("Starting Elite Titan Casino v9.0...");
        config = new CasinoConfig();
        adapter = new DreamBotAdapter();
        gameManager = new GameManager(config);
        startTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() {
        if (!adapter.isLoggedIn()) return 5000;

        if (Trade.isOpen()) {
            handleTrade();
        } else {
            sentSafetyMsg = false;
            // Proactively check for trade requests
            // DreamBot automatically handles the "Accept" if we call Trade.isOpen() or similar
            // but we want to be sure we're looking for it.
            
            // Advertising logic
            if (Calculations.random(0, 100) < 2) {
                adapter.sendMessage(config.adMessage);
            }
        }

        return Calculations.random(400, 800);
    }

    private void handleTrade() {
        String currentPlayer = Trade.getTradingWith();
        if (currentPlayer != null) {
            lastPlayer = currentPlayer;
        }

        if (Trade.isOpen(1)) {
            if (!sentSafetyMsg) {
                adapter.sendTradeChatMessage(config.tradeWelcomeMsg);
                Sleep.sleep(1000, 1500);
                adapter.sendTradeChatMessage(config.tradeSafetyMsg);
                sentSafetyMsg = true;
            }

            Item[] items = Trade.getTheirItems();
            int offered = 0;
            if (items != null) {
                for (Item item : items) {
                    if (item != null && item.getID() == 995) {
                        offered += item.getAmount();
                    }
                }
            }

            if (offered >= config.minBet && offered <= config.maxBet) {
                // In DreamBot 3/4, we can check if the other player has accepted by looking at the trade window state
                // or simply call acceptTrade() which will wait for them if needed.
                // A common pattern is to check if we can accept.
                Trade.acceptTrade();
            }
        } else if (Trade.isOpen(2)) {
            Trade.acceptTrade();
            // Wait for trade to finish
            Sleep.sleepUntil(() -> !Trade.isOpen(), 5000);
            
            // Resolve game
            // In a real scenario, we'd parse the game type from chat or GUI
            // For now, we default to Craps as requested
            resolveGame("craps", 1000000, lastPlayer);
        }
    }

    private void resolveGame(String gameType, int betAmount, String playerName) {
        com.ikingsnipe.casino.games.GameResult result = gameManager.play(gameType, betAmount);
        if (result.isWin()) {
            wins++;
            adapter.sendMessage(String.format(config.winMsg, playerName, result.getPayout(), result.getOutcome()));
            // Payout logic would go here (opening trade back to player)
        } else {
            losses++;
            adapter.sendMessage(String.format(config.lossMsg, playerName, result.getOutcome()));
        }
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(5, 5, 250, 120);
        g.setColor(Color.CYAN);
        g.drawRect(5, 5, 250, 120);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Elite Titan Casino v9.0", 15, 25);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Status: " + (Trade.isOpen() ? "TRADING" : "ADVERTISING"), 15, 45);
        g.drawString("Last Player: " + lastPlayer, 15, 60);
        g.drawString("Wins: " + wins + " | Losses: " + losses, 15, 75);
        
        long elapsed = System.currentTimeMillis() - startTime;
        g.drawString("Runtime: " + formatTime(elapsed), 15, 95);
        
        int totalGames = wins + losses;
        double winRate = totalGames == 0 ? 0 : (double) wins / totalGames * 100;
        g.drawString(String.format("Win Rate: %.2f%%", winRate), 15, 110);
    }

    private String formatTime(long ms) {
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
    }
}
