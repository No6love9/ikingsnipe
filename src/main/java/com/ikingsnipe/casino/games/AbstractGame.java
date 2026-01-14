package com.ikingsnipe.casino.games;

import java.util.Map;
import java.util.Random;

public abstract class AbstractGame {
    protected final Random random = new Random();

    public abstract GameResult play(int betAmount, Map<String, Object> config);
    public abstract String getRules();
    public abstract boolean validateBet(int amount);
    public abstract int calculatePayout(boolean win, int betAmount);
}
