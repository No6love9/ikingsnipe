package com.ikingsnipe.casino.utils;

import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.utilities.Sleep;

public class DreamBotAdapter {
    
    public void speak(String message) {
        Keyboard.type(message + "\n");
    }

    public void speakInTrade(String message) {
        // Widget 335 is the trade window. Child 53 is the chat input.
        WidgetChild tradeChat = Widgets.getWidgetChild(335, 53);
        if (tradeChat != null && tradeChat.isVisible()) {
            Keyboard.type(message + "\n");
        } else {
            speak(message);
        }
    }

    public boolean acceptTradeRequest(String playerName) {
        Player p = Players.closest(playerName);
        if (p != null) {
            return p.interact("Trade with");
        }
        return false;
    }

    public boolean isTradeOpen() {
        return Trade.isOpen();
    }

    public boolean isTradeOpen(int window) {
        return Trade.isOpen(window);
    }

    public void forceClose() {
        if (Trade.isOpen()) {
            Trade.declineTrade();
        }
        Widgets.closeAll();
    }
}
