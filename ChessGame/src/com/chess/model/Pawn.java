package com.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    private static final long serialVersionUID = 1L;

    public Pawn(PieceColor color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        // White moves toward row 0 (dir = -1); black moves toward row 7 (dir = +1)
        int dir        = (color == PieceColor.WHITE) ? -1 : 1;
        int startRow   = (color == PieceColor.WHITE) ? 6 : 1;
        int promoteRow = (color == PieceColor.WHITE) ? 0 : 7;
        int r = from.getRow(), c = from.getCol();

        // ── Single push ───────────────────────────────────────────────────────
        Position oneStep = new Position(r + dir, c);
        if (oneStep.isValid() && board.getPiece(oneStep) == null) {
            if (oneStep.getRow() == promoteRow) {
                addPromotionMoves(from, oneStep, false, moves);
            } else {
                moves.add(new Move(from, oneStep, Move.MoveType.NORMAL));

                // ── Double push from starting square ──────────────────────────
                if (r == startRow) {
                    Position twoStep = new Position(r + 2 * dir, c);
                    if (board.getPiece(twoStep) == null) {
                        moves.add(new Move(from, twoStep, Move.MoveType.NORMAL));
                    }
                }
            }
        }

        // ── Diagonal captures ─────────────────────────────────────────────────
        for (int dc : new int[]{-1, 1}) {
            Position capturePos = new Position(r + dir, c + dc);
            if (!capturePos.isValid()) continue;

            Piece target = board.getPiece(capturePos);
            if (target != null && target.getColor() != color) {
                if (capturePos.getRow() == promoteRow) {
                    addPromotionMoves(from, capturePos, true, moves);
                } else {
                    moves.add(new Move(from, capturePos, Move.MoveType.CAPTURE));
                }
            }

            // ── En passant ────────────────────────────────────────────────────
            Position ep = board.getEnPassantTarget();
            if (ep != null && ep.equals(capturePos)) {
                moves.add(new Move(from, capturePos, Move.MoveType.EN_PASSANT));
            }
        }

        return moves;
    }

    @Override
    public Piece copy() {
        Pawn copy = new Pawn(color);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
