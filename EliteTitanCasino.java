package com.ikingsnipe;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import com.ikingsnipe.casino.CasinoController;
import com.ikingsnipe.casino.gui.CasinoPanel;
import javax.swing.*;
import java.awt.*;

/**
 * Elite Titan Casino Pro - Complete Casino Host System
 * Production-grade implementation with full GUI control and robust error handling
 */
@ScriptManifest(
    name = "Elite Titan Casino Pro",
    author = "ikingsnipe",
    version = 2.0,
    description = "Complete casino system with Craps, Dice Duel, and Flower Poker",
    category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript {
    
    private CasinoController controller;
    private CasinoPanel gui;
    
    @Override
    public void onStart() {
        log("================================================");
        log("    Elite Titan Casino Pro v2.0 Initializing    ");
        log("================================================");
        
        try {
            // Initialize casino controller
            controller = new CasinoController(this);
            
            // Initialize GUI on Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                gui = new CasinoPanel(controller);
                gui.setVisible(true);
                gui.log("GUI initialized successfully");
            });
            
            log("Casino system ready. Use the control panel to start.");
            
        } catch (Exception e) {
            log("CRITICAL ERROR during initialization: " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }
    
    @Override
    public int onLoop() {
        if (controller == null || !controller.isRunning()) {
            return 1000;
        }
        
        try {
            // Process casino operations
            controller.process();
            
            // Update paint overlay
            if (controller.shouldPaint()) {
                controller.updatePaint();
            }
            
            return 300; // Fast loop for responsive trading
            
        } catch (Exception e) {
            log("Error in main loop: " + e.getMessage());
            controller.handleError(e);
            return 3000;
        }
    }
    
    @Override
    public void onPaint(Graphics g) {
        if (controller != null && controller.isRunning()) {
            controller.onPaint(g);
        } else {
            // Display stopped state
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(5, 5, 250, 100);
            g.setColor(Color.WHITE);
            g.drawString("Elite Titan Casino Pro", 15, 25);
            g.drawString("Status: STOPPED", 15, 45);
            g.drawString("Open GUI to start", 15, 65);
        }
    }
    
    @Override
    public void onExit() {
        log("Shutting down Elite Titan Casino...");
        
        if (controller != null) {
            controller.shutdown();
        }
        
        if (gui != null) {
            SwingUtilities.invokeLater(() -> gui.dispose());
        }
        
        log("Casino system shutdown complete");
        log("================================================");
    }
    
    // Public getter for GUI access
    public CasinoController getController() {
        return controller;
    }
}