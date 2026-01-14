package com.ikingsnipe;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.List;

/**
 * Elite Titan Casino Script - Robust implementation with comprehensive error handling
 * Compatible with DreamBot 3
 */
@ScriptManifest(
        name = "Elite Titan Casino",
        description = "Advanced casino gambling script with robust error handling",
        author = "ikingsnipe",
        version = 1.0,
        category = Category.MISC
)
public class EliteTitanCasino extends AbstractScript {

    // Configuration constants
    private static final int SCRIPT_VERSION = 1;
    private static final long TIMEOUT_DURATION = 30000; // 30 seconds
    private static final int MAX_RETRIES = 3;
    
    // Casino game definitions
    private static final int DICE_GAME_ID = 10403;
    private static final int SPIN_WHEEL_ID = 10404;
    private static final int ROULETTE_TABLE_ID = 10405;
    
    // Coin IDs for various games
    private static final int COINS_ID = 995;
    
    // Game areas (with fallback coordinates)
    private static final Area DICE_AREA = new Area(3281, 3269, 3289, 3261, 0);
    private static final Area WHEEL_AREA = new Area(3252, 3275, 3267, 3260, 0);
    private static final Area ROULETTE_AREA = new Area(3296, 3282, 3313, 3273, 0);
    private static final Area CASINO_LOBBY = new Area(3240, 3290, 3320, 3250, 0);
    
    // Script state variables
    private GameState currentGameState;
    private long lastActivityTime;
    private int successfulGames;
    private int failedAttempts;
    private int totalCoinsGambled;
    
    enum GameState {
        IDLE,
        NAVIGATING,
        INTERACTING,
        GAMING,
        BANKING,
        ERROR_RECOVERY
    }

    @Override
    public void onStart() {
        log("Elite Titan Casino script v" + SCRIPT_VERSION + " starting...");
        currentGameState = GameState.IDLE;
        lastActivityTime = System.currentTimeMillis();
        successfulGames = 0;
        failedAttempts = 0;
        totalCoinsGambled = 0;
    }

    @Override
    public int onLoop() {
        try {
            lastActivityTime = System.currentTimeMillis();
            
            if (!validateGameState()) {
                return handleGameStateError();
            }
            
            switch (getCurrentGameState()) {
                case IDLE:
                    return handleIdleState();
                case NAVIGATING:
                    return handleNavigatingState();
                case INTERACTING:
                    return handleInteractingState();
                case GAMING:
                    return handleGamingState();
                case BANKING:
                    return handleBankingState();
                case ERROR_RECOVERY:
                    return handleErrorRecovery();
                default:
                    return Calculations.random(500, 1000);
            }
        } catch (Exception e) {
            log("Unexpected error in main loop: " + e.getMessage());
            currentGameState = GameState.ERROR_RECOVERY;
            return handleErrorRecovery();
        }
    }

    private boolean validateGameState() {
        Player player = Players.getLocal();
        if (player == null) {
            return false;
        }
        
        if (player.isAnimating() && isAnimationTimeout()) {
            return false;
        }
        
        return true;
    }

    private int handleIdleState() {
        Player player = Players.getLocal();
        if (player == null) {
            return Calculations.random(500, 1000);
        }
        
        Item coins = Inventory.get(COINS_ID);
        if (coins == null || coins.getAmount() < 100) {
            currentGameState = GameState.BANKING;
            return Calculations.random(1000, 2000);
        }
        
        if (!isInCasinoArea(player)) {
            currentGameState = GameState.NAVIGATING;
        } else {
            currentGameState = GameState.INTERACTING;
        }
        
        return Calculations.random(500, 1000);
    }

    private int handleNavigatingState() {
        Player player = Players.getLocal();
        if (player == null) {
            return Calculations.random(500, 1000);
        }
        
        if (isInCasinoArea(player)) {
            currentGameState = GameState.INTERACTING;
            return Calculations.random(500, 1000);
        }
        
        if (!Walking.walk(CASINO_LOBBY.getRandomTile())) {
            failedAttempts++;
            if (failedAttempts > MAX_RETRIES) {
                currentGameState = GameState.ERROR_RECOVERY;
            }
        }
        
        return Calculations.random(1000, 2000);
    }

    private int handleInteractingState() {
        Player player = Players.getLocal();
        if (player == null) {
            return Calculations.random(500, 1000);
        }
        
        int gameChoice = Calculations.random(1, 3);
        GameObject gameObject = null;
        
        switch (gameChoice) {
            case 1:
                gameObject = findNearestGameObject(DICE_GAME_ID, DICE_AREA);
                break;
            case 2:
                gameObject = findNearestGameObject(SPIN_WHEEL_ID, WHEEL_AREA);
                break;
            case 3:
                gameObject = findNearestGameObject(ROULETTE_TABLE_ID, ROULETTE_AREA);
                break;
        }
        
        if (gameObject != null && gameObject.interact("Play")) {
            currentGameState = GameState.GAMING;
            failedAttempts = 0;
            return Calculations.random(1000, 3000);
        } else {
            failedAttempts++;
            if (failedAttempts > MAX_RETRIES) {
                currentGameState = GameState.ERROR_RECOVERY;
            }
            return Calculations.random(500, 1500);
        }
    }

    private int handleGamingState() {
        Player player = Players.getLocal();
        if (player == null) {
            currentGameState = GameState.ERROR_RECOVERY;
            return Calculations.random(1000, 2000);
        }
        
        if (player.isAnimating() || player.isMoving()) {
            return Calculations.random(500, 1500);
        }
        
        Item coins = Inventory.get(COINS_ID);
        if (coins != null) {
            int coinsAfter = coins.getAmount();
            totalCoinsGambled += Math.abs(coinsAfter - totalCoinsGambled);
            successfulGames++;
        }
        
        currentGameState = GameState.IDLE;
        return Calculations.random(1000, 3000);
    }

    private int handleBankingState() {
        log("Banking state - coin restocking not implemented in this version");
        currentGameState = GameState.IDLE;
        return Calculations.random(2000, 4000);
    }

    private int handleErrorRecovery() {
        Player player = Players.getLocal();
        if (player == null) {
            return Calculations.random(2000, 4000);
        }
        
        if (player.isAnimating()) {
            return Calculations.random(1000, 2000);
        }
        
        currentGameState = GameState.IDLE;
        failedAttempts = 0;
        return Calculations.random(2000, 4000);
    }

    private GameObject findNearestGameObject(int gameObjectId, Area area) {
        List<GameObject> gameObjects = GameObjects.all(obj -> obj != null && obj.getID() == gameObjectId && area.contains(obj));
        if (gameObjects == null || gameObjects.isEmpty()) {
            return null;
        }
        
        GameObject nearest = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (GameObject obj : gameObjects) {
            double distance = obj.distance();
            if (distance < closestDistance) {
                closestDistance = distance;
                nearest = obj;
            }
        }
        
        return nearest;
    }

    private boolean isInCasinoArea(Player player) {
        if (player == null) {
            return false;
        }
        
        return CASINO_LOBBY.contains(player) ||
               DICE_AREA.contains(player) ||
               WHEEL_AREA.contains(player) ||
               ROULETTE_AREA.contains(player);
    }

    private GameState getCurrentGameState() {
        return currentGameState != null ? currentGameState : GameState.IDLE;
    }

    private boolean isAnimationTimeout() {
        return (System.currentTimeMillis() - lastActivityTime) > TIMEOUT_DURATION;
    }

    private int handleGameStateError() {
        currentGameState = GameState.ERROR_RECOVERY;
        return Calculations.random(1000, 2000);
    }

    @Override
    public void onExit() {
        log("Script stopping...");
        log("Statistics - Successful games: " + successfulGames + 
            ", Failed attempts: " + failedAttempts + 
            ", Total coins gambled: " + totalCoinsGambled);
    }
}
