package com.ikingsnipe.framework.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for intermediate nodes in the tree structure.
 */
public abstract class Branch implements Leaf {
    protected final List<Leaf> children = new ArrayList<>();

    /**
     * Adds a child node (Branch or Leaf) to this branch.
     * @param child the child to add.
     * @return this branch for chaining.
     */
    public Branch addChild(Leaf child) {
        children.add(child);
        return this;
    }

    @Override
    public int onLoop() {
        for (Leaf child : children) {
            if (child.isValid()) {
                return child.onLoop();
            }
        }
        return 600; // Default delay if no child is valid
    }
}
