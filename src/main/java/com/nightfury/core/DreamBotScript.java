package com.nightfury.core;

import com.nightfury.core.logic.TradeManager;
import com.nightfury.core.persistence.DatabaseManager;
import com.nightfury.core.discord.DiscordController;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(
    name = "iKingSnipe - Manus Edition",
    author = "iKingSnipe",
    version = 1.0,
    category = Category.MISC,
    description = "Enterprise Trade & Sniper System"
)
public class DreamBotScript extends AbstractScript {

    private TradeManager tradeManager;

    @Override
    public void onStart() {
        Logger.log("iKingSnipe - Manus System Integration Initializing...");

        try {
            // 1. Initialize Persistence Layer
            DatabaseManager.init();

            // 2. Initialize Core Trade Logic
            tradeManager = new TradeManager();
            tradeManager.init();

            // 3. Initialize Discord C2 Controller
            DiscordController.setTradeManager(tradeManager);
            DiscordController.init();

            Logger.log("iKingSnipe - Manus System Integration is Running.");
        } catch (Exception e) {
            Logger.log("Error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int onLoop() {
        // The main logic is handled by the TradeManager's tick loop.
        // We return a sleep time for the DreamBot loop.
        return 600; 
    }

    @Override
    public void onExit() {
        Logger.log("Shutting down iKingSnipe - Manus System...");
        try {
            DatabaseManager.shutdown();
        } catch (Exception e) {
            Logger.log("Error during shutdown: " + e.getMessage());
        }
    }
}
