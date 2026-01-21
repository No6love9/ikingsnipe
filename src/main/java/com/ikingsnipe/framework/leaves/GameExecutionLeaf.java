package com.ikingsnipe.framework.leaves;

import com.ikingsnipe.framework.core.Leaf;
import com.ikingsnipe.casino.managers.GameManager;
import com.ikingsnipe.casino.managers.SessionManager;

/**
 * Leaf responsible for executing the actual casino games logic.
 */
public class GameExecutionLeaf implements Leaf {
    private final GameManager gameManager;
    private final SessionManager sessionManager;

    public GameExecutionLeaf(GameManager gameManager, SessionManager sessionManager) {
        this.gameManager = gameManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean isValid() {
        // Valid if there are active players in the session queue
        return sessionManager.hasActiveSessions();
    }

    @Override
    public int onLoop() {
        gameManager.processActiveGames();
        return 600;
    }
}
