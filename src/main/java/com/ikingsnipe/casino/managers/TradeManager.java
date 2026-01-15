package com.ikingsnipe.casino.managers;

import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.wrappers.items.Item;

public class TradeManager {
    public boolean validateTrade(int expectedAmount) {
        if (!Trade.isOpen(1)) return false;
        Item[] items = Trade.getTheirItems();
        int offered = 0;
        if (items != null) {
            for (Item item : items) {
                if (item != null && item.getID() == 995) {
                    offered += item.getAmount();
                }
            }
        }
        return offered >= expectedAmount;
    }

    public void accept() {
        Trade.acceptTrade();
    }

    public void decline() {
        Trade.declineTrade();
    }
}
