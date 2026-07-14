package com.chess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Piece implements Serializable {
    private static final long serialVersionUID = 1L;

    protected PieceColor color;
    protected PieceType  type;
    protected boolean    hasMoved;

    protected Piece(PieceColor color, PieceType type) {
        this.color    = color;
        this.type     = type;
        this.hasMoved = false;
    }

    /** Returns pseudo-legal moves (may leave own king in check). */
    public abstract List<Move> getPseudoLegalMoves(Board board, Position from);

    // ── Getters / setters ─────────────────────────────────────────────────────
    public PieceColor getColor()          { return color; }
    public PieceType  getType()           { return type; }
    public boolean    hasMoved()          { return hasMoved; }
    public void       setHasMoved(boolean b) { hasMoved = b; }

    /** Returns the Unicode symbol for this piece. */
    public char getSymbol() {
        return switch (type) {
            case KING   -> color == PieceColor.WHITE ? '♔' : '♚';
            case QUEEN  -> color == PieceColor.WHITE ? '♕' : '♛';
            case ROOK   -> color == PieceColor.WHITE ? '♖' : '♜';
            case BISHOP -> color == PieceColor.WHITE ? '♗' : '♝';
            case KNIGHT -> color == PieceColor.WHITE ? '♘' : '♞';
            case PAWN   -> color == PieceColor.WHITE ? '♙' : '♟';
        };
    }

    /** Creates a deep copy of this piece. */
    public abstract Piece copy();

    // ── Helpers for subclasses ────────────────────────────────────────────────

    /** Adds moves along a ray direction until blocked or off board. */
    protected void addRayMoves(Board board, Position from, int dr, int dc, List<Move> moves) {
        for (int dist = 1; dist < 8; dist++) {
            Position to = from.offset(dr * dist, dc * dist);
            if (!to.isValid()) break;
            Piece target = board.getPiece(to);
            if (target == null) {
                moves.add(new Move(from, to, Move.MoveType.NORMAL));
            } else {
                if (target.getColor() != color) {
                    moves.add(new Move(from, to, Move.MoveType.CAPTURE));
                }
                break; // blocked
            }
        }
    }

    /** Adds a single step move if the target square is empty or has an enemy piece. */
    protected void addStepMove(Board board, Position from, int dr, int dc, List<Move> moves) {
        Position to = from.offset(dr, dc);
        if (!to.isValid()) return;
        Piece target = board.getPiece(to);
        if (target == null) {
            moves.add(new Move(from, to, Move.MoveType.NORMAL));
        } else if (target.getColor() != color) {
            moves.add(new Move(from, to, Move.MoveType.CAPTURE));
        }
    }

    /** Builds promotion moves (one per promotable piece type). */
    protected void addPromotionMoves(Position from, Position to, boolean isCapture, List<Move> moves) {
        Move.MoveType mt = isCapture ? Move.MoveType.PROMOTION_CAPTURE : Move.MoveType.PROMOTION;
        for (PieceType pt : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT}) {
            moves.add(new Move(from, to, mt, pt));
        }
    }
}
