package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    private static final long serialVersionUID = 1L;

    private static final int[][] OFFSETS = {
        {-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}
    };

    public Knight(PieceColor color) {
        super(color, PieceType.KNIGHT);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        for (int[] off : OFFSETS) {
            addStepMove(board, from, off[0], off[1], moves);
        }
        return moves;
    }

    @Override
    public Piece copy() {
        Knight copy = new Knight(color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
