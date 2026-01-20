package com.ikingsnipe.casino.managers;

import org.dreambot.api.methods.input.Keyboard;


import org.dreambot.api.utilities.Logger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Robust Game Engine for GoatGang Edition
 * Handles all game logic, streaks, and history tracking.
 */
public class GameEngine {
    private static final List<String> gameHistory = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY = 10;

    public enum GameType {
        DICE_DUEL("Dice Duel"),
        CRAPS("Craps"),
        MID("Mid"),
        OVER("Over"),
        UNDER("Under"),
        BLACKJACK("Blackjack");

        private final String name;
        GameType(String name) { this.name = name; }
        public String getName() { return name; }
    }

    /**
     * Processes a game roll and records history
     */
    public static GameResult play(GameType type, String player, long bet) {
        int roll = new Random().nextInt(100) + 1;
        boolean win = false;
        String detail = "";

        switch (type) {
            case DICE_DUEL:
                int playerRoll = new Random().nextInt(100) + 1;
                int botRoll = new Random().nextInt(100) + 1;
                win = playerRoll > botRoll;
                detail = "P:" + playerRoll + " vs B:" + botRoll;
                break;
            case CRAPS:
                win = (roll == 7 || roll == 11);
                detail = "Roll: " + roll;
                break;
            case MID:
                win = (roll >= 40 && roll <= 60);
                detail = "Roll: " + roll;
                break;
            case OVER:
                win = (roll > 55);
                detail = "Roll: " + roll;
                break;
            case UNDER:
                win = (roll < 45);
                detail = "Roll: " + roll;
                break;
        }

        GameResult result = new GameResult(player, type, bet, win, detail, roll);
        addHistory(result);
        return result;
    }

    private static void addHistory(GameResult result) {
        String entry = (result.win ? "W" : "L") + ":" + result.type.getName();
        gameHistory.add(0, entry);
        if (gameHistory.size() > MAX_HISTORY) {
            gameHistory.remove(gameHistory.size() - 1);
        }
    }

    public static List<String> getRecentStreaks() {
        return new ArrayList<>(gameHistory);
    }

    public static class GameResult {
        public String player;
        public GameType type;
        public long bet;
        public boolean win;
        public String detail;
        public int roll;

        public GameResult(String player, GameType type, long bet, boolean win, String detail, int roll) {
            this.player = player; this.type = type; this.bet = bet; this.win = win; this.detail = detail; this.roll = roll;
        }
    }
}
