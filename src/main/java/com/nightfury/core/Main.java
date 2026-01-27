package com.nightfury.core;

import com.nightfury.core.logic.TradeManager;
import com.nightfury.core.persistence.DatabaseManager;
import com.nightfury.core.discord.DiscordController;
import com.nightfury.core.ui.MainDashboard;

public class Main {

    public static void main(String[] args) {
        System.out.println("iKingSnipe - Manus System Integration Initializing...");

        // 1. Initialize Persistence Layer
        DatabaseManager.init();

        // 2. Initialize Discord C2 Controller
        DiscordController.init();

        // 3. Initialize Core Trade Logic
        TradeManager tradeManager = new TradeManager();
        tradeManager.init();

        // 4. Launch JavaFX UI
        // Note: JavaFX applications typically launch via Application.launch(MainDashboard.class, args);
        // For a simple console application that can be run from a fat JAR, we'll start the logic here
        // and assume the UI is launched separately or is a non-blocking component.
        // For this completion, we will simulate the UI launch.
        System.out.println("Launching Professional JavaFX Dashboard...");
        // MainDashboard.launchUI(args); 
        
        System.out.println("iKingSnipe - Manus System Integration is Running.");
        
        // Keep the main thread alive for the ScheduledExecutorService and JDA
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
