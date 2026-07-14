package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
    private static final long serialVersionUID = 1L;

    public Bishop(PieceColor color) {
        super(color, PieceType.BISHOP);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        addRayMoves(board, from,  1,  1, moves);
        addRayMoves(board, from,  1, -1, moves);
        addRayMoves(board, from, -1,  1, moves);
        addRayMoves(board, from, -1, -1, moves);
        return moves;
    }

    @Override
    public Piece copy() {
        Bishop copy = new Bishop(color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
