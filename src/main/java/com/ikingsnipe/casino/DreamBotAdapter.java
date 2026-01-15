package com.ikingsnipe.casino.utils;

import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.chat.Chat;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.utilities.Sleep;

/**
 * Adapter class for DreamBot API calls
 * Provides abstraction and error handling for all DreamBot interactions
 */
public class DreamBotAdapter {
    
    private final AbstractScript script;
    
    public DreamBotAdapter(AbstractScript script) {
        this.script = script;
    }
    
    // === TRADE METHODS ===
    
    public boolean isTradeOpen() {
        try {
            return Trade.isOpen();
        } catch (Exception e) {
            script.log("Error checking trade state: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isFirstTradeScreen() {
        try {
            return Trade.isOpen(1);
        } catch (Exception e) {
            script.log("Error checking first trade screen: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isSecondTradeScreen() {
        try {
            return Trade.isOpen(2);
        } catch (Exception e) {
            script.log("Error checking second trade screen: " + e.getMessage());
            return false;
        }
    }
    
    public String getTradePartner() {
        try {
            return Trade.getPartner();
        } catch (Exception e) {
            script.log("Error getting trade partner: " + e.getMessage());
            return null;
        }
    }
    
    public boolean openTrade(String playerName) {
        try {
            script.log("Attempting to open trade with " + playerName);
            boolean success = Trade.openTrade(playerName);
            
            if (success) {
                Sleep.sleepUntil(() -> Trade.isOpen(), 5000);
            }
            
            return success;
        } catch (Exception e) {
            script.log("Error opening trade with " + playerName + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean acceptTrade() {
        try {
            return Trade.acceptTrade();
        } catch (Exception e) {
            script.log("Error accepting trade: " + e.getMessage());
            return false;
        }
    }
    
    public boolean declineTrade() {
        try {
            return Trade.declineTrade();
        } catch (Exception e) {
            script.log("Error declining trade: " + e.getMessage());
            return false;
        }
    }
    
    public boolean addItemToTrade(int itemId, int amount) {
        try {
            return Trade.addItem(itemId, amount);
        } catch (Exception e) {
            script.log("Error adding item to trade: " + e.getMessage());
            return false;
        }
    }
    
    public int getOfferedItemAmount(int itemId) {
        try {
            Item[] theirItems = Trade.getTheirItems();
            if (theirItems == null) return 0;
            
            int total = 0;
            for (Item item : theirItems) {
                if (item != null && item.getID() == itemId) {
                    total += item.getAmount();
                }
            }
            return total;
        } catch (Exception e) {
            script.log("Error getting offered item amount: " + e.getMessage());
            return 0;
        }
    }
    
    public boolean waitForTradeComplete(int timeout) {
        try {
            return Sleep.sleepUntil(() -> !Trade.isOpen(), timeout);
        } catch (Exception e) {
            script.log("Error waiting for trade complete: " + e.getMessage());
            return false;
        }
    }
    
    // === INVENTORY METHODS ===
    
    public int getItemCount(int itemId) {
        try {
            Item item = Inventory.get(itemId);
            return item != null ? item.getAmount() : 0;
        } catch (Exception e) {
            script.log("Error getting item count: " + e.getMessage());
            return 0;
        }
    }
    
    public boolean hasItem(int itemId, int amount) {
        return getItemCount(itemId) >= amount;
    }
    
    // === BANK METHODS ===
    
    public boolean withdrawFromBank(int itemId, int amount, String pin) {
        try {
            if (!Bank.isOpen()) {
                if (!Bank.open()) {
                    return false;
                }
                Sleep.sleepUntil(() -> Bank.isOpen(), 3000);
            }
            
            // Handle bank pin if needed
            if (Bank.isPinEnabled() && pin != null && !pin.isEmpty()) {
                Bank.enterPin(pin);
            }
            
            // Withdraw item
            boolean success = Bank.withdraw(itemId, amount);
            Sleep.sleep(500);
            
            // Close bank
            Bank.close();
            
            return success;
        } catch (Exception e) {
            script.log("Error withdrawing from bank: " + e.getMessage());
            return false;
        }
    }
    
    // === CHAT METHODS ===
    
    public boolean sendPublicMessage(String message) {
        try {
            return Chat.send(message);
        } catch (Exception e) {
            script.log("Error sending public message: " + e.getMessage());
            return false;
        }
    }
    
    public boolean sendPrivateMessage(String player, String message) {
        try {
            String fullMessage = player + ": " + message;
            return Chat.send(fullMessage);
        } catch (Exception e) {
            script.log("Error sending private message to " + player + ": " + e.getMessage());
            return false;
        }
    }
    
    public String getPlayerName() {
        try {
            return Players.getLocal().getName();
        } catch (Exception e) {
            script.log("Error getting player name: " + e.getMessage());
            return "Unknown";
        }
    }
    
    // === WIDGET METHODS ===
    
    public boolean closeAllWidgets() {
        try {
            Widgets.closeAll();
            return true;
        } catch (Exception e) {
            script.log("Error closing widgets: " + e.getMessage());
            return false;
        }
    }
    
    // === SYSTEM METHODS ===
    
    public boolean initialize() {
        // Validate DreamBot environment
        try {
            // Check if we're logged in
            if (Players.getLocal() == null) {
                script.log("Player not logged in");
                return false;
            }
            
            // Check if we have inventory access
            if (Inventory.isEmpty()) {
                // This is fine, just log it
                script.log("Inventory is empty");
            }
            
            return true;
        } catch (Exception e) {
            script.log("Error initializing DreamBot adapter: " + e.getMessage());
            return false;
        }
    }
    
    public void emergencyStop() {
        // Close any open trades
        if (isTradeOpen()) {
            declineTrade();
        }
        
        // Close widgets
        closeAllWidgets();
        
        script.log("DreamBotAdapter emergency stop executed");
    }
    
    // === UTILITY METHODS ===
    
    public void sleep(int ms) {
        try {
            Sleep.sleep(ms);
        } catch (Exception e) {
            // Ignore sleep interruptions
        }
    }
    
    public boolean sleepUntil(org.dreambot.api.utilities.Condition condition, int timeout) {
        try {
            return Sleep.sleepUntil(condition, timeout);
        } catch (Exception e) {
            script.log("Error in sleepUntil: " + e.getMessage());
            return false;
        }
    }
}