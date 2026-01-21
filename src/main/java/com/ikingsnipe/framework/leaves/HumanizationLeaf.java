package com.ikingsnipe.framework.leaves;

import com.ikingsnipe.framework.core.Leaf;
import com.ikingsnipe.casino.managers.HumanizationManager;

/**
 * Leaf responsible for anti-ban and humanization.
 */
public class HumanizationLeaf implements Leaf {
    private final HumanizationManager humanizationManager;

    public HumanizationLeaf(HumanizationManager humanizationManager) {
        this.humanizationManager = humanizationManager;
    }

    @Override
    public boolean isValid() {
        return humanizationManager.shouldTakeBreak() || humanizationManager.isOnBreak();
    }

    @Override
    public int onLoop() {
        if (humanizationManager.shouldTakeBreak()) {
            humanizationManager.takeBreak();
        } else {
            humanizationManager.applyIdleHumanization();
        }
        return 600;
    }
}
