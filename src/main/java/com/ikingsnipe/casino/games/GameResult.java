package com.ikingsnipe.casino.games;
public class GameResult {
    private final boolean win; private final long payout; private final String resultDescription, rawOutcome;
    public GameResult(boolean win, long payout, String desc, String raw) { this.win = win; this.payout = payout; this.resultDescription = desc; this.rawOutcome = raw; }
    public boolean isWin() { return win; } public long getPayout() { return payout; }
    public String getResultDescription() { return resultDescription; } public String getRawOutcome() { return rawOutcome; }
}
