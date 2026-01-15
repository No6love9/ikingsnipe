package com.ikingsnipe.casino.games;

public class GameResult {
    private final boolean win;
    private final int outcome;
    private final int payout;

    public GameResult(boolean win, int outcome, int payout) {
        this.win = win;
        this.outcome = outcome;
        this.payout = payout;
    }

    public boolean isWin() { return win; }
    public int getOutcome() { return outcome; }
    public int getPayout() { return payout; }
}
