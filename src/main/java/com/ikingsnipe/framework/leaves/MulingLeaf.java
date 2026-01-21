package com.ikingsnipe.framework.leaves;

import com.ikingsnipe.framework.core.Leaf;
import com.ikingsnipe.casino.managers.MuleManager;

/**
 * Leaf responsible for handling muling operations.
 */
public class MulingLeaf implements Leaf {
    private final MuleManager muleManager;

    public MulingLeaf(MuleManager muleManager) {
        this.muleManager = muleManager;
    }

    @Override
    public boolean isValid() {
        return muleManager.shouldMule() || muleManager.isMulingInProgress();
    }

    @Override
    public int onLoop() {
        muleManager.performMule();
        return 600;
    }
}
