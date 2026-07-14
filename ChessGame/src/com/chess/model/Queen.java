package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
    private static final long serialVersionUID = 1L;

    public Queen(PieceColor color) {
        super(color, PieceType.QUEEN);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        // Rook directions
        addRayMoves(board, from,  0,  1, moves);
        addRayMoves(board, from,  0, -1, moves);
        addRayMoves(board, from,  1,  0, moves);
        addRayMoves(board, from, -1,  0, moves);
        // Bishop directions
        addRayMoves(board, from,  1,  1, moves);
        addRayMoves(board, from,  1, -1, moves);
        addRayMoves(board, from, -1,  1, moves);
        addRayMoves(board, from, -1, -1, moves);
        return moves;
    }

    @Override
    public Piece copy() {
        Queen copy = new Queen(color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
