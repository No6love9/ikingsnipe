package com.ikingsnipe.casino.utils;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

public class DreamBotAdapter {
    private static final int COINS_ID = 995;

    public boolean isLoggedIn() {
        return Client.getGameState() == GameState.LOGGED_IN;
    }

    public boolean openTrade(String playerName) {
        Player player = Players.closest(playerName);
        return player != null && player.interact("Trade with");
    }

    public boolean isInTrade() {
        return Trade.isOpen();
    }

    public boolean hasCoins(int amount) {
        Item coins = Inventory.get(COINS_ID);
        return coins != null && coins.getAmount() >= amount;
    }

    public void sendMessage(String message) {
        Keyboard.type(message + "\n");
    }

    public void sendTradeChatMessage(String message) {
        // Widget 335 is the first trade window, child 53 is the chat input area
        WidgetChild chatInput = Widgets.getWidgetChild(335, 53);
        if (chatInput != null && chatInput.isVisible()) {
            Keyboard.type(message + "\n");
        } else {
            // Fallback to regular chat if trade chat isn't focused
            sendMessage(message);
        }
    }

    public String getPendingTradePlayer() {
        // DreamBot doesn't have a direct "getTradeRequester" but we can check the chat for "wishes to trade"
        // Or more reliably, check if the trade window is being requested via the Trade class
        return null; // Logic handled in main loop via Trade.isOpen() or interaction
    }
}
