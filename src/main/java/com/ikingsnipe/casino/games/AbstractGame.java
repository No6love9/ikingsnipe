package com.ikingsnipe.casino.games;
import java.util.Map; import java.util.Random;
public abstract class AbstractGame {
    protected final Random random = new Random();
    public abstract GameResult play(long betAmount, Map<String, Object> config);
    public abstract String getRules();
}
