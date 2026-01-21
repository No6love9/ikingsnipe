package com.ikingsnipe.framework.leaves;

import com.ikingsnipe.framework.core.Leaf;
import com.ikingsnipe.casino.managers.TradeManager;
import org.dreambot.api.methods.trade.Trade;

/**
 * Leaf responsible for handling trade interactions.
 */
public class TradeLeaf implements Leaf {
    private final TradeManager tradeManager;

    public TradeLeaf(TradeManager tradeManager) {
        this.tradeManager = tradeManager;
    }

    @Override
    public boolean isValid() {
        return Trade.isOpen();
    }

    @Override
    public int onLoop() {
        if (Trade.isOpen(1)) {
            tradeManager.handleTradeScreen1();
        } else if (Trade.isOpen(2)) {
            tradeManager.handleTradeScreen2();
        }
        return 600;
    }
}
