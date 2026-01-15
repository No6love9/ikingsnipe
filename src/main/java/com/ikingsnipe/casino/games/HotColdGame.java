package com.ikingsnipe.casino.games;
import java.util.Map;
public class HotColdGame extends AbstractGame {
    public GameResult play(long bet, Map<String, Object> config) {
        boolean win = random.nextBoolean(); String o = random.nextBoolean() ? "Hot" : "Cold";
        double m = (double) config.getOrDefault("payoutMultiplier", 2.0);
        return new GameResult(win, win ? (long)(bet * m) : 0, (win ? "correct! " : "incorrect. ") + "Outcome: " + o, o);
    }
    public String getRules() { return "50/50 chance."; }
}
