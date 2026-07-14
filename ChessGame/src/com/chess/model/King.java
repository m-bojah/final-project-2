package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    private static final long serialVersionUID = 1L;

    // Castling-rights indices: [WK=0, WQ=1, BK=2, BQ=3]
    private static final int WK = 0, WQ = 1, BK = 2, BQ = 3;

    public King(PieceColor color) {
        super(color, PieceType.KING);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();

        // ── Normal king moves ─────────────────────────────────────────────────
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                addStepMove(board, from, dr, dc, moves);
            }
        }

        // ── Castling ──────────────────────────────────────────────────────────
        if (!hasMoved) {
            int row = from.getRow();
            boolean[] cr = board.getCastlingRights();
            PieceColor enemy = color.opposite();

            // Kingside
            int kingIdx = (color == PieceColor.WHITE) ? WK : BK;
            if (cr[kingIdx]
                    && board.getPiece(new Position(row, 5)) == null
                    && board.getPiece(new Position(row, 6)) == null
                    && !board.isAttackedBy(new Position(row, 4), enemy)
                    && !board.isAttackedBy(new Position(row, 5), enemy)
                    && !board.isAttackedBy(new Position(row, 6), enemy)) {
                moves.add(new Move(from, new Position(row, 6), Move.MoveType.CASTLING_KINGSIDE));
            }

            // Queenside
            int queenIdx = (color == PieceColor.WHITE) ? WQ : BQ;
            if (cr[queenIdx]
                    && board.getPiece(new Position(row, 1)) == null
                    && board.getPiece(new Position(row, 2)) == null
                    && board.getPiece(new Position(row, 3)) == null
                    && !board.isAttackedBy(new Position(row, 4), enemy)
                    && !board.isAttackedBy(new Position(row, 3), enemy)
                    && !board.isAttackedBy(new Position(row, 2), enemy)) {
                moves.add(new Move(from, new Position(row, 2), Move.MoveType.CASTLING_QUEENSIDE));
            }
        }

        return moves;
    }

    @Override
    public Piece copy() {
        King copy = new King(color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
