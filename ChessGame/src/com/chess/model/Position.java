package com.chess.model;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public Position offset(int dr, int dc) {
        return new Position(row + dr, col + dc);
    }

    /** Converts to chess algebraic notation (e.g. "e4") */
    @Override
    public String toString() {
        return "" + (char) ('a' + col) + (8 - row);
    }

    public static Position fromAlgebraic(String s) {
        int col = s.charAt(0) - 'a';
        int row = 8 - (s.charAt(1) - '0');
        return new Position(row, col);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
