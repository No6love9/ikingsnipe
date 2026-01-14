package com.ikingsnipe.casino.utils;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

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
        Keyboard.type(message);
    }
}
