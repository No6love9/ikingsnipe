package com.ikingsnipe.core;

import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.database.DatabaseManager;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;

@ScriptManifest(name = "GoatGang Edition", author = "iKingSnipe", version = 14.0, category = Category.MISC, description = "Enterprise Casino Framework")
public class BotApplication extends AbstractScript {
    private CasinoConfig config;
    private CasinoGUI gui;
    private boolean started = false;

    @Override
    public void onStart() {
        if (!SecurityManager.authenticate()) {
            stop();
            return;
        }

        config = CasinoConfig.load();
        DatabaseManager.setup(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPass);
        DiscordManager.initBot(config.discordBotToken);

        gui = new CasinoGUI(config, (start) -> {
            this.started = start;
            Logger.log("[GoatGang] Framework initialized and started.");
        });
        gui.setVisible(true);
    }

    @Override
    public int onLoop() {
        if (!started) return 1000;

        handleLocation();
        return 600;
    }

    private void handleLocation() {
        if (config.getTargetTile() != null && !Area.generateArea(5, config.getTargetTile()).contains(Players.getLocal())) {
            Walking.walk(config.getTargetTile());
        }
    }

    public void onMessage(Message msg) {
        if (!started) return;

        String text = msg.getMessage().toLowerCase();
        String sender = msg.getUsername();

        if (text.startsWith(config.cmdDiceDuel)) handleGame(GameEngine.GameType.DICE_DUEL, sender);
        else if (text.startsWith(config.cmdCraps)) handleGame(GameEngine.GameType.CRAPS, sender);
        else if (text.startsWith(config.cmdMid)) handleGame(GameEngine.GameType.MID, sender);
        else if (text.startsWith(config.cmdOver)) handleGame(GameEngine.GameType.OVER, sender);
        else if (text.startsWith(config.cmdUnder)) handleGame(GameEngine.GameType.UNDER, sender);
        else if (text.startsWith(config.cmdBalance)) {
            long bal = DatabaseManager.getBalance(sender);
            Logger.log(String.format(config.msgBalance, sender, BalanceManager.formatGP(bal)));
        } else if (text.startsWith(config.cmdGoatGang)) {
            Logger.log(String.format(config.msgStreaks, GameEngine.getRecentStreaks().toString()));
        }
    }

    private void handleGame(GameEngine.GameType type, String player) {
        try {
            long balance = DatabaseManager.getBalance(player);
            if (balance < config.minBet) {
                Logger.log(String.format("[GoatGang] %s has insufficient balance: %s GP", player, BalanceManager.formatGP(balance)));
                return;
            }

            long betAmount = Math.min(balance, config.maxBet);
            GameEngine.GameResult result = GameEngine.play(type, player, betAmount);

            if (result.win) {
                long payout = (long)(betAmount * config.payoutMultiplier);
                DatabaseManager.updateBalance(player, balance + payout);
                Logger.log(String.format(config.msgWin, player, BalanceManager.formatGP(payout), result.detail));
                TradeManager.sendPayout(player, payout);
            } else {
                DatabaseManager.updateBalance(player, balance - betAmount);
                Logger.log(String.format(config.msgLoss, player, result.detail));
            }

            ProfitTracker.recordGame(result.win, betAmount);
        } catch (Exception e) {
            Logger.error("[GoatGang] Error handling game for " + player + ": " + e.getMessage());
        }
    }

    @Override
    public void onPaint(Graphics g) {
        if (started) {
            g.setColor(new Color(212, 175, 55));
            g.drawString("GoatGang Edition v14.0", 10, 50);
            g.drawString("Status: Operational", 10, 70);
            g.drawString("Recent: " + GameEngine.getRecentStreaks(), 10, 90);
        }
    }

    @Override
    public void onExit() {
        DatabaseManager.shutdown();
        if (gui != null) gui.dispose();
    }
}
