package com.ikingsnipe.framework.leaves;

import com.ikingsnipe.framework.core.Leaf;
import com.ikingsnipe.casino.managers.BankingManager;

/**
 * Leaf responsible for handling banking and restocking.
 */
public class BankingLeaf implements Leaf {
    private final BankingManager bankingManager;

    public BankingLeaf(BankingManager bankingManager) {
        this.bankingManager = bankingManager;
    }

    @Override
    public boolean isValid() {
        return bankingManager.shouldBank();
    }

    @Override
    public int onLoop() {
        bankingManager.restock();
        return 600;
    }
}
