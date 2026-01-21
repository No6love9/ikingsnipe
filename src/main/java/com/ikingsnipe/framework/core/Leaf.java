package com.ikingsnipe.framework.core;

/**
 * Base interface for all executable actions in the framework.
 */
public interface Leaf {
    /**
     * Checks if this leaf should be executed.
     * @return true if valid, false otherwise.
     */
    boolean isValid();

    /**
     * The main logic to execute when this leaf is active.
     * @return the delay in milliseconds before the next loop.
     */
    int onLoop();
}
