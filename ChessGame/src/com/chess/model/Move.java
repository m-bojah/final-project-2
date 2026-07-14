package com.chess.model;

import java.io.Serializable;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MoveType {
        NORMAL, CAPTURE, CASTLING_KINGSIDE, CASTLING_QUEENSIDE,
        EN_PASSANT, PROMOTION, PROMOTION_CAPTURE
    }

    // Move specification
    private final Position from;
    private final Position to;
    private final MoveType type;
    private final PieceType promotionType; // only for PROMOTION / PROMOTION_CAPTURE

    // Undo state — filled in by Board.makeMove()
    private Piece movedPiece;
    private Piece capturedPiece;
    private Position capturedPos;        // differs from 'to' for en passant
    private boolean pieceWasFirstMove;   // whether the moved piece had hasMoved==false
    private boolean rookWasFirstMove;    // for castling undo
    private boolean[] prevCastlingRights;
    private Position prevEnPassantTarget;

    // Algebraic notation (e.g. "e2e4", "e7e8q")
    private String algebraicNotation;

    public Move(Position from, Position to) {
        this(from, to, MoveType.NORMAL, null);
    }

    public Move(Position from, Position to, MoveType type) {
        this(from, to, type, null);
    }

    public Move(Position from, Position to, MoveType type, PieceType promotionType) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.promotionType = promotionType;
    }

    // ── Specification getters ──────────────────────────────────────────────────
    public Position getFrom()           { return from; }
    public Position getTo()             { return to; }
    public MoveType getType()           { return type; }
    public PieceType getPromotionType() { return promotionType; }

    public boolean isCapture() {
        return type == MoveType.CAPTURE || type == MoveType.EN_PASSANT
                || type == MoveType.PROMOTION_CAPTURE;
    }

    public boolean isPromotion() {
        return type == MoveType.PROMOTION || type == MoveType.PROMOTION_CAPTURE;
    }

    public boolean isCastling() {
        return type == MoveType.CASTLING_KINGSIDE || type == MoveType.CASTLING_QUEENSIDE;
    }

    // ── Undo-state getters / setters ───────────────────────────────────────────
    public Piece getMovedPiece()                            { return movedPiece; }
    public void  setMovedPiece(Piece p)                     { movedPiece = p; }

    public Piece getCapturedPiece()                         { return capturedPiece; }
    public void  setCapturedPiece(Piece p)                  { capturedPiece = p; }

    public Position getCapturedPos()                        { return capturedPos; }
    public void     setCapturedPos(Position p)              { capturedPos = p; }

    public boolean  pieceWasFirstMove()                     { return pieceWasFirstMove; }
    public void     setPieceWasFirstMove(boolean b)         { pieceWasFirstMove = b; }

    public boolean  rookWasFirstMove()                      { return rookWasFirstMove; }
    public void     setRookWasFirstMove(boolean b)          { rookWasFirstMove = b; }

    public boolean[] getPrevCastlingRights()                { return prevCastlingRights; }
    public void      setPrevCastlingRights(boolean[] r)     { prevCastlingRights = r.clone(); }

    public Position getPrevEnPassantTarget()                { return prevEnPassantTarget; }
    public void     setPrevEnPassantTarget(Position p)      { prevEnPassantTarget = p; }

    public String getAlgebraicNotation()                    { return algebraicNotation; }
    public void   setAlgebraicNotation(String s)            { algebraicNotation = s; }

    @Override
    public String toString() {
        if (algebraicNotation != null) return algebraicNotation;
        String s = from.toString() + to.toString();
        if (isPromotion() && promotionType != null) s += promotionType.name().substring(0, 1).toLowerCase();
        return s;
    }
}
