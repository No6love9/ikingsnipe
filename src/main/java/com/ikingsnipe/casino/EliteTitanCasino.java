package com.ikingsnipe.casino;

import com.ikingsnipe.casino.core.CasinoState;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.utils.DreamBotAdapter;
import com.ikingsnipe.casino.utils.ProvablyFair;
import com.ikingsnipe.casino.gui.CasinoPanel;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Sleep;

import javax.swing.*;
import java.awt.*;

@ScriptManifest(
    name = "Elite Titan Casino v12.0",
    description = "Ultra-robust casino host with Multi-Currency support, full configurability, and GUI control.",
    author = "ikingsnipe",
    version = 12.0,
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript {

    private CasinoConfig config;
    private DreamBotAdapter adapter;
    private ProvablyFair pf;
    private CasinoPanel gui;
    private CasinoState state = CasinoState.IDLE;
    
    private String currentPlayer = null;
    private long stateStartTime;
    private long scriptStartTime;
    private long lastAdTime;
    private boolean messagedWelcome = false;
    
    private int wins = 0;
    private int losses = 0;

    @Override
    public void onStart() {
        log("Initializing Elite Titan Casino v12.0...");
        config = new CasinoConfig();
        adapter = new DreamBotAdapter();
        pf = new ProvablyFair();
        
        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            gui = new CasinoPanel(config);
            gui.setVisible(true);
        });

        state = CasinoState.IDLE;
        stateStartTime = System.currentTimeMillis();
        scriptStartTime = System.currentTimeMillis();
        lastAdTime = 0;
    }

    @Override
    public int onLoop() {
        if (state != CasinoState.IDLE && System.currentTimeMillis() - stateStartTime > config.tradeTimeoutMs) {
            log("State timeout: " + state + ". Resetting...");
            resetState();
            return 1000;
        }

        try {
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
        } catch (Exception e) {
            log("Critical error in loop: " + e.getMessage());
            resetState();
        }

        return Calculations.random(config.loopDelayMinMs, config.loopDelayMaxMs);
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

        if (System.currentTimeMillis() - lastAdTime > config.adIntervalMs) {
            adapter.speak(config.adMessage);
            lastAdTime = System.currentTimeMillis();
        }

        if (config.autoRestock && getInventoryValue() < config.restockThreshold) {
            handleRestock();
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
            Sleep.sleep(config.messageDelayMinMs, config.messageDelayMaxMs);
            adapter.speakInTrade(config.tradeSafety);
            messagedWelcome = true;
        }

        long offeredValue = getOfferedValue();
        if (offeredValue >= config.minBet && offeredValue <= config.maxBet) {
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
        
        int total = pf.generateRoll(2, 12);
        CasinoConfig.GameSettings settings = config.games.get("craps");
        boolean win = settings.winningNumbers.contains(total);
        
        if (win) {
            wins++;
            adapter.speak(String.format(config.winAnnouncement, currentPlayer, total, 3000000, pf.revealServerSeed().substring(0, 8)));
        } else {
            losses++;
            adapter.speak(String.format(config.lossAnnouncement, currentPlayer, total));
        }
        
        resetState();
    }

    private void handleRestock() {
        if (Bank.open()) {
            Bank.withdrawAll(CasinoConfig.COINS_ID);
            Bank.withdrawAll(CasinoConfig.PLATINUM_TOKEN_ID);
            Sleep.sleepUntil(() -> getInventoryValue() > config.restockThreshold, 5000);
            Bank.close();
        }
    }

    private long getOfferedValue() {
        Item[] items = Trade.getTheirItems();
        long totalValue = 0;
        if (items != null) {
            for (Item item : items) {
                if (item != null) {
                    if (item.getID() == CasinoConfig.COINS_ID) {
                        totalValue += item.getAmount();
                    } else if (item.getID() == CasinoConfig.PLATINUM_TOKEN_ID) {
                        totalValue += (long) item.getAmount() * CasinoConfig.TOKEN_VALUE;
                    }
                }
            }
        }
        return totalValue;
    }

    private long getInventoryValue() {
        long coins = Inventory.count(CasinoConfig.COINS_ID);
        long tokens = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
        return coins + (tokens * CasinoConfig.TOKEN_VALUE);
    }

    private void resetState() {
        adapter.forceClose();
        state = CasinoState.IDLE;
        currentPlayer = null;
        messagedWelcome = false;
        stateStartTime = System.currentTimeMillis();
    }

    @Override
    public void onExit() {
        if (gui != null) {
            gui.dispose();
        }
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRoundRect(10, 30, 300, 180, 15, 15);
        g2.setColor(new Color(255, 215, 0));
        g2.drawRoundRect(10, 30, 300, 180, 15, 15);
        
        g2.setFont(new Font("Verdana", Font.BOLD, 16));
        g2.drawString("ELITE TITAN CASINO v12.0", 25, 55);
        
        g2.setFont(new Font("Verdana", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("State: " + state, 25, 80);
        g2.drawString("Player: " + (currentPlayer != null ? currentPlayer : "None"), 25, 100);
        g2.drawString("Value: " + getInventoryValue() + " GP", 25, 120);
        g2.drawString("Wins: " + wins + " | Losses: " + losses, 25, 140);
        g2.drawString("Provably Fair: ACTIVE", 25, 160);
        
        long elapsed = System.currentTimeMillis() - scriptStartTime;
        g2.drawString("Runtime: " + formatTime(elapsed), 25, 180);
        
        g2.setColor(state == CasinoState.IDLE ? Color.GREEN : Color.YELLOW);
        g2.fillOval(280, 40, 15, 15);
    }

    private String formatTime(long ms) {
        long s = ms / 1000;
        long m = s / 60;
        long h = m / 60;
        return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
    }
}
