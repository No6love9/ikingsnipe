package com.ikingsnipe.core;

import com.ikingsnipe.framework.core.TreeScript;
import com.ikingsnipe.framework.branches.*;
import com.ikingsnipe.framework.leaves.*;
import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.database.DatabaseManager;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.widgets.message.Message;
import java.awt.*;

@ScriptManifest(
    name = "GoatGang Edition", 
    author = "iKingSnipe", 
    version = 14.0, 
    category = Category.MISC, 
    description = "Enterprise Casino Framework - Tree-Branch-Leaf Edition"
)
public class BotApplication extends TreeScript {
    private CasinoConfig config;
    private CasinoGUI gui;
    private boolean started = false;

    // Managers
    private TradeManager tradeManager;
    private BankingManager bankingManager;
    private MuleManager muleManager;
    private HumanizationManager humanizationManager;
    private LocationManager locationManager;

    @Override
    public void onStart() {
        if (!SecurityManager.authenticate()) {
            stop();
            return;
        }

        config = CasinoConfig.load();
        DatabaseManager.setup(config.dbHost, config.dbPort, config.dbName, config.dbUser, config.dbPass);
        
        // Initialize Managers
        tradeManager = new TradeManager(config, null); 
        bankingManager = new BankingManager(config);
        muleManager = new MuleManager(config);
        humanizationManager = new HumanizationManager(config);
        locationManager = new LocationManager(config);

        // Initialize Framework Tree
        initializeTree();

        gui = new CasinoGUI(config, (start) -> {
            this.started = start;
            Logger.log("[GoatGang] Framework initialized and started.");
        });
        gui.setVisible(true);
    }

    private void initializeTree() {
        // 1. Humanization (Highest Priority)
        getRoot().addChild(new HumanizationLeaf(humanizationManager));

        // 2. Maintenance (Banking/Muling)
        MaintenanceBranch maintenance = new MaintenanceBranch();
        maintenance.addChild(new MulingLeaf(muleManager));
        maintenance.addChild(new BankingLeaf(bankingManager));
        getRoot().addChild(maintenance);

        // 3. Hosting (Core Logic)
        HostingBranch hosting = new HostingBranch(locationManager);
        hosting.addChild(new TradeLeaf(tradeManager));
        hosting.addChild(new GameExecutionLeaf(new GameManager(config), new SessionManager()));
        hosting.addChild(new AutoChatLeaf(config));
        getRoot().addChild(hosting);
    }

    @Override
    public int onLoop() {
        if (!started) return 1000;
        return super.onLoop();
    }

    @Override
    public void onMessage(Message msg) {
        if (!started) return;
    }

    @Override
    public void onPaint(Graphics g) {
        if (started) {
            g.setColor(new Color(212, 175, 55));
            g.drawString("GoatGang Edition v14.0", 10, 50);
            g.drawString("Status: Operational (Tree-Branch-Leaf)", 10, 70);
        }
    }

    @Override
    public void onExit() {
        DatabaseManager.shutdown();
        if (gui != null) gui.dispose();
    }
}
