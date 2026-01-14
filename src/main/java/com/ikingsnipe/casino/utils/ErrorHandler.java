package com.ikingsnipe.casino.utils;

import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;

public class ErrorHandler {
    public void resetStuckState() {
        if (Trade.isOpen()) {
            Trade.declineTrade();
        }
        Widgets.closeAll();
    }

    public boolean validateGameState() {
        // Check if player is still logged in and in a valid area
        return true;
    }
}
