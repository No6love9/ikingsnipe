package com.ikingsnipe.framework.core;

import org.dreambot.api.script.AbstractScript;

/**
 * Base class for scripts using the Tree-Branch-Leaf framework.
 */
public abstract class TreeScript extends AbstractScript {
    private final Branch root = new RootBranch();

    @Override
    public int onLoop() {
        if (root.isValid()) {
            return root.onLoop();
        }
        return 1000;
    }

    /**
     * Gets the root branch to add top-level branches.
     * @return the root branch.
     */
    public Branch getRoot() {
        return root;
    }

    private static class RootBranch extends Branch {
        @Override
        public boolean isValid() {
            return true;
        }
    }
}
