package com.nightfury.core;

import com.nightfury.core.logic.TradeManager;
import com.nightfury.core.persistence.DatabaseManager;
import com.nightfury.core.discord.DiscordController;

public class Main {

    public static void main(String[] args) {
        System.out.println("iKingSnipe - Manus System Integration Initializing...");

        // 1. Initialize Persistence Layer
        DatabaseManager.init();

        // 2. Initialize Core Trade Logic
        TradeManager tradeManager = new TradeManager();
        tradeManager.init();

        // 3. Initialize Discord C2 Controller
        DiscordController.setTradeManager(tradeManager);
        DiscordController.init();

        System.out.println("iKingSnipe - Manus System Integration is Running (Headless Mode).");
        
        // Keep the main thread alive
        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
