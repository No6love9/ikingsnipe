package com.ikingsnipe.casino.games;

import java.util.Random;

public abstract class AbstractGame {
    protected final Random random = new Random();
    public abstract GameResult play(String player, long bet, String seed);
}
