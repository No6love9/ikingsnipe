package com.ikingsnipe.casino;

import com.ikingsnipe.casino.core.CasinoState;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.utils.DreamBotAdapter;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Sleep;

import java.awt.*;

@ScriptManifest(
    name = "Elite Titan Casino v11.0",
    description = "Ultra-advanced casino host with Provably Fair RNG and Bank Integration.",
    author = "ikingsnipe",
    version = 11.0,
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript {

    private CasinoConfig config;
    private DreamBotAdapter adapter;
    private ProvablyFair pf;
    private CasinoState state = CasinoState.IDLE;
    
    private String currentPlayer = null;
    private long stateStartTime;
    private long scriptStartTime;
    private boolean messagedWelcome = false;
    
    private int wins = 0;
    private int losses = 0;
    private long totalProfit = 0;

    @Override
    public void onStart() {
        log("Initializing Elite Titan Casino v11.0...");
        config = new CasinoConfig();
        adapter = new DreamBotAdapter();
        pf = new ProvablyFair();
        state = CasinoState.IDLE;
        stateStartTime = System.currentTimeMillis();
        scriptStartTime = System.currentTimeMillis();
    }

    @Override
    public int onLoop() {
        if (state != CasinoState.IDLE && System.currentTimeMillis() - stateStartTime > config.tradeTimeoutMs) {
            resetState();
            return 1000;
        }

        switch (state) {
            case IDLE:
                handleIdle();
                break;
            case TRADING_WINDOW_1:
                handleTradeWindow1();
                break;
            case TRADING_WINDOW_2:
                handleTradeWindow2();
                break;
            case PROCESSING_GAME:
                handleGameResolution();
                break;
        }

        return Calculations.random(200, 400);
    }

    private void handleIdle() {
        if (Trade.isOpen()) {
            currentPlayer = Trade.getTradingWith();
            if (config.blacklist.contains(currentPlayer)) {
                adapter.speak(String.format(config.blacklistMsg, currentPlayer));
                Trade.declineTrade();
                return;
            }
            state = CasinoState.TRADING_WINDOW_1;
            stateStartTime = System.currentTimeMillis();
            messagedWelcome = false;
            return;
        }

        // Auto-Restock Check
        if (config.autoRestock && !Trade.isOpen() && getInventoryCoins() < config.restockThreshold) {
            handleRestock();
        }

        if (Calculations.random(0, 100) < 1) {
            adapter.speak(config.adMessage);
        }
    }

    private void handleTradeWindow1() {
        if (!Trade.isOpen(1)) {
            resetState();
            return;
        }

        if (!messagedWelcome) {
            pf.setClientSeed(currentPlayer + System.currentTimeMillis());
            adapter.speakInTrade(String.format(config.tradeWelcome, pf.getServerSeedHash().substring(0, 10)));
            Sleep.sleep(1000, 1500);
            adapter.speakInTrade(config.tradeSafety);
            messagedWelcome = true;
        }

        int offered = getOfferedAmount();
        if (offered >= config.minBet && offered <= config.maxBet) {
            Trade.acceptTrade();
            if (Trade.isOpen(2)) {
                state = CasinoState.TRADING_WINDOW_2;
                stateStartTime = System.currentTimeMillis();
            }
        }
    }

    private void handleTradeWindow2() {
        if (!Trade.isOpen(2)) {
            if (!Trade.isOpen()) resetState();
            return;
        }

        Trade.acceptTrade();
        if (!Trade.isOpen()) {
            state = CasinoState.PROCESSING_GAME;
            stateStartTime = System.currentTimeMillis();
        }
    }

    private void handleGameResolution() {
        Sleep.sleepUntil(() -> !Trade.isOpen(), 3000);
        
        // Provably Fair Roll
        int total = pf.generateRoll(2, 12);
        
        java.util.List<Integer> winsList = (java.util.List<Integer>) config.gameSettings.get("craps_wins");
        boolean win = winsList.contains(total);
        
        if (win) {
            wins++;
            int payout = 3000000; // Example
            adapter.speak(String.format(config.winAnnouncement, currentPlayer, total, payout, pf.revealServerSeed().substring(0, 8)));
        } else {
            losses++;
            adapter.speak(String.format(config.lossAnnouncement, currentPlayer, total));
        }
        
        resetState();
    }

    private void handleRestock() {
        if (Bank.open()) {
            Bank.withdrawAll(995);
            Sleep.sleepUntil(() -> getInventoryCoins() > config.restockThreshold, 5000);
            Bank.close();
        }
    }

    private int getOfferedAmount() {
        Item[] items = Trade.getTheirItems();
        int total = 0;
        if (items != null) {
            for (Item item : items) {
                if (item != null && item.getID() == 995) total += item.getAmount();
            }
        }
        return total;
    }

    private int getInventoryCoins() {
        return (int) org.dreambot.api.methods.container.impl.Inventory.count(995);
    }

    private void resetState() {
        adapter.forceClose();
        state = CasinoState.IDLE;
        currentPlayer = null;
        messagedWelcome = false;
        stateStartTime = System.currentTimeMillis();
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRoundRect(10, 30, 280, 160, 15, 15);
        g2.setColor(new Color(0, 255, 255));
        g2.drawRoundRect(10, 30, 280, 160, 15, 15);
        
        g2.setFont(new Font("Verdana", Font.BOLD, 16));
        g2.drawString("ELITE TITAN CASINO v11.0", 25, 55);
        
        g2.setFont(new Font("Verdana", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("State: " + state, 25, 80);
        g2.drawString("Player: " + (currentPlayer != null ? currentPlayer : "None"), 25, 100);
        g2.drawString("Wins: " + wins + " | Losses: " + losses, 25, 120);
        g2.drawString("Provably Fair: ACTIVE", 25, 140);
        
        long elapsed = System.currentTimeMillis() - scriptStartTime;
        g2.drawString("Runtime: " + formatTime(elapsed), 25, 160);
        
        g2.setColor(state == CasinoState.IDLE ? Color.GREEN : Color.CYAN);
        g2.fillOval(260, 40, 15, 15);
    }

    private String formatTime(long ms) {
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
    }
}
