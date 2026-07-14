package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    private static final long serialVersionUID = 1L;

    public Rook(PieceColor color) {
        super(color, PieceType.ROOK);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        addRayMoves(board, from,  0,  1, moves);
        addRayMoves(board, from,  0, -1, moves);
        addRayMoves(board, from,  1,  0, moves);
        addRayMoves(board, from, -1,  0, moves);
        return moves;
    }

    @Override
    public Piece copy() {
        Rook copy = new Rook(color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
