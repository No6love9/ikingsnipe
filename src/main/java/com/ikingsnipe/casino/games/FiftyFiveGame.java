package com.ikingsnipe.casino.games;
import java.util.Map;
public class FiftyFiveGame extends AbstractGame {
    public GameResult play(long bet, Map<String, Object> config) {
        int r = random.nextInt(100) + 1; boolean win = r >= 55;
        double m = (double) config.getOrDefault("payoutMultiplier", 2.0);
        return new GameResult(win, win ? (long)(bet * m) : 0, "rolled " + r, String.valueOf(r));
    }
    public String getRules() { return "55+ wins."; }
}
