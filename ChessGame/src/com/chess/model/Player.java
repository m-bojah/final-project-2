package com.chess.model;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String     name;
    private final PieceColor color;
    private final boolean    isHuman;
    private int              timeSeconds; // remaining time

    public Player(String name, PieceColor color, boolean isHuman, int timeSeconds) {
        this.name        = name;
        this.color       = color;
        this.isHuman     = isHuman;
        this.timeSeconds = timeSeconds;
    }

    public String     getName()        { return name; }
    public PieceColor getColor()       { return color; }
    public boolean    isHuman()        { return isHuman; }
    public int        getTimeSeconds() { return timeSeconds; }
    public void       setTimeSeconds(int t) { timeSeconds = t; }

    public void decrementTime() { if (timeSeconds > 0) timeSeconds--; }
    public boolean isOutOfTime() { return timeSeconds <= 0; }

    public String getFormattedTime() {
        int m = timeSeconds / 60;
        int s = timeSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
