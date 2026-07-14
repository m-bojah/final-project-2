package com.chess.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the chess board and enforces all official chess rules.
 *
 * Grid layout: grid[row][col], row 0 = rank 8 (black's back rank), row 7 = rank 1 (white's back rank).
 * White pieces occupy rows 6–7 at start; black pieces occupy rows 0–1.
 */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    // Castling-rights indices
    private static final int WK = 0, WQ = 1, BK = 2, BQ = 3;

    private Piece[][]  grid;
    private PieceColor currentPlayer;
    private Position   enPassantTarget;   // square a pawn can capture via en passant
    private boolean[]  castlingRights;    // [WK, WQ, BK, BQ]
    private Position   whiteKingPos;
    private Position   blackKingPos;

    // ── Construction ──────────────────────────────────────────────────────────

    public Board() {
        grid           = new Piece[8][8];
        castlingRights = new boolean[]{true, true, true, true};
        currentPlayer  = PieceColor.WHITE;
        setupInitialPosition();
    }

    /** Private constructor for deep copy. */
    private Board(boolean empty) {
        grid           = new Piece[8][8];
        castlingRights = new boolean[4];
    }

    private void setupInitialPosition() {
        // Black pieces (row 0)
        grid[0][0] = new Rook(PieceColor.BLACK);
        grid[0][1] = new Knight(PieceColor.BLACK);
        grid[0][2] = new Bishop(PieceColor.BLACK);
        grid[0][3] = new Queen(PieceColor.BLACK);
        grid[0][4] = new King(PieceColor.BLACK);
        grid[0][5] = new Bishop(PieceColor.BLACK);
        grid[0][6] = new Knight(PieceColor.BLACK);
        grid[0][7] = new Rook(PieceColor.BLACK);
        // Black pawns (row 1)
        for (int c = 0; c < 8; c++) grid[1][c] = new Pawn(PieceColor.BLACK);
        // White pawns (row 6)
        for (int c = 0; c < 8; c++) grid[6][c] = new Pawn(PieceColor.WHITE);
        // White pieces (row 7)
        grid[7][0] = new Rook(PieceColor.WHITE);
        grid[7][1] = new Knight(PieceColor.WHITE);
        grid[7][2] = new Bishop(PieceColor.WHITE);
        grid[7][3] = new Queen(PieceColor.WHITE);
        grid[7][4] = new King(PieceColor.WHITE);
        grid[7][5] = new Bishop(PieceColor.WHITE);
        grid[7][6] = new Knight(PieceColor.WHITE);
        grid[7][7] = new Rook(PieceColor.WHITE);

        whiteKingPos = new Position(7, 4);
        blackKingPos = new Position(0, 4);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Piece getPiece(Position pos) {
        return grid[pos.getRow()][pos.getCol()];
    }

    public void setPiece(Position pos, Piece piece) {
        grid[pos.getRow()][pos.getCol()] = piece;
    }

    public PieceColor getCurrentPlayer()      { return currentPlayer; }
    public Position   getEnPassantTarget()    { return enPassantTarget; }
    public boolean[]  getCastlingRights()     { return castlingRights; }
    public Position   getWhiteKingPos()       { return whiteKingPos; }
    public Position   getBlackKingPos()       { return blackKingPos; }

    // ── Move generation ───────────────────────────────────────────────────────

    /** Returns all legal moves for the current player. */
    public List<Move> getLegalMoves() {
        List<Move> legal = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor() == currentPlayer) {
                    Position pos = new Position(r, c);
                    legal.addAll(getLegalMovesForPiece(pos));
                }
            }
        }
        return legal;
    }

    /** Returns all legal moves for the piece at the given position. */
    public List<Move> getLegalMovesForPiece(Position pos) {
        Piece p = getPiece(pos);
        if (p == null || p.getColor() != currentPlayer) return List.of();
        List<Move> legal = new ArrayList<>();
        for (Move m : p.getPseudoLegalMoves(this, pos)) {
            if (isLegal(m)) legal.add(m);
        }
        return legal;
    }

    private boolean isLegal(Move m) {
        PieceColor movingSide = currentPlayer;
        makeMove(m);
        boolean safe = !isKingInCheck(movingSide);
        undoMove(m);
        return safe;
    }

    // ── Make / Unmake ─────────────────────────────────────────────────────────

    public void makeMove(Move move) {
        Position from = move.getFrom();
        Position to   = move.getTo();
        int fr = from.getRow(), fc = from.getCol();
        int tr = to.getRow(),   tc = to.getCol();

        Piece moving = grid[fr][fc];

        // Save undo state
        move.setMovedPiece(moving);
        move.setCapturedPiece(grid[tr][tc]);
        move.setCapturedPos(to);
        move.setPieceWasFirstMove(!moving.hasMoved());
        move.setPrevEnPassantTarget(enPassantTarget);
        move.setPrevCastlingRights(castlingRights);

        // Basic move
        grid[tr][tc] = moving;
        grid[fr][fc] = null;
        moving.setHasMoved(true);
        enPassantTarget = null;

        switch (move.getType()) {
            case EN_PASSANT -> {
                // Captured pawn is on the same row as 'from', same col as 'to'
                Position capPos = new Position(fr, tc);
                move.setCapturedPiece(grid[fr][tc]);
                move.setCapturedPos(capPos);
                grid[fr][tc] = null;
            }
            case CASTLING_KINGSIDE -> {
                Piece rook = grid[tr][7];
                move.setRookWasFirstMove(!rook.hasMoved());
                grid[tr][5] = rook;
                grid[tr][7] = null;
                rook.setHasMoved(true);
            }
            case CASTLING_QUEENSIDE -> {
                Piece rook = grid[tr][0];
                move.setRookWasFirstMove(!rook.hasMoved());
                grid[tr][3] = rook;
                grid[tr][0] = null;
                rook.setHasMoved(true);
            }
            case PROMOTION, PROMOTION_CAPTURE -> {
                Piece promoted = createPiece(move.getPromotionType(), moving.getColor());
                promoted.setHasMoved(true);
                grid[tr][tc] = promoted;
            }
            default -> {
                // Track en passant opportunity from a pawn double-push
                if (moving.getType() == PieceType.PAWN && Math.abs(tr - fr) == 2) {
                    enPassantTarget = new Position((fr + tr) / 2, fc);
                }
            }
        }

        // Update castling rights
        if (moving.getType() == PieceType.KING) {
            if (moving.getColor() == PieceColor.WHITE) { castlingRights[WK] = false; castlingRights[WQ] = false; }
            else                                        { castlingRights[BK] = false; castlingRights[BQ] = false; }
        }
        revokeCastlingRight(from);
        revokeCastlingRight(to);

        // Track king position
        if (moving.getType() == PieceType.KING) {
            if (moving.getColor() == PieceColor.WHITE) whiteKingPos = to;
            else                                        blackKingPos = to;
        }

        currentPlayer = currentPlayer.opposite();
    }

    public void undoMove(Move move) {
        Position from = move.getFrom();
        Position to   = move.getTo();
        int fr = from.getRow(), fc = from.getCol();
        int tr = to.getRow(),   tc = to.getCol();

        currentPlayer = currentPlayer.opposite();

        Piece moving = move.getMovedPiece();

        // Restore piece to original square
        grid[fr][fc] = moving;
        grid[tr][tc] = null;
        if (move.pieceWasFirstMove()) moving.setHasMoved(false);

        switch (move.getType()) {
            case EN_PASSANT -> {
                // Restore the captured pawn (was not at 'to', but at capturedPos)
                Position capPos = move.getCapturedPos();
                grid[capPos.getRow()][capPos.getCol()] = move.getCapturedPiece();
            }
            case CASTLING_KINGSIDE -> {
                Piece rook = grid[tr][5];
                grid[tr][7] = rook;
                grid[tr][5] = null;
                if (move.rookWasFirstMove()) rook.setHasMoved(false);
            }
            case CASTLING_QUEENSIDE -> {
                Piece rook = grid[tr][3];
                grid[tr][0] = rook;
                grid[tr][3] = null;
                if (move.rookWasFirstMove()) rook.setHasMoved(false);
            }
            default -> {
                // Restore captured piece at 'to'
                grid[tr][tc] = move.getCapturedPiece();
            }
        }

        // Restore board state
        enPassantTarget = move.getPrevEnPassantTarget();
        castlingRights  = move.getPrevCastlingRights().clone();

        // Restore king position
        if (moving.getType() == PieceType.KING) {
            if (moving.getColor() == PieceColor.WHITE) whiteKingPos = from;
            else                                        blackKingPos = from;
        }
    }

    // ── Check / Game-state detection ──────────────────────────────────────────

    public boolean isKingInCheck(PieceColor color) {
        Position kingPos = (color == PieceColor.WHITE) ? whiteKingPos : blackKingPos;
        if (kingPos == null) return false;
        return isAttackedBy(kingPos, color.opposite());
    }

    public GameState getGameState() {
        boolean inCheck      = isKingInCheck(currentPlayer);
        boolean hasLegal     = !getLegalMoves().isEmpty();
        if (!hasLegal) return inCheck ? GameState.CHECKMATE : GameState.STALEMATE;
        return inCheck ? GameState.CHECK : GameState.PLAYING;
    }

    /**
     * Returns true if the given square is attacked by any piece of the given color.
     */
    public boolean isAttackedBy(Position pos, PieceColor attackerColor) {
        int r = pos.getRow(), c = pos.getCol();

        // Pawn attacks: white pawns attack "upward" (from higher row), black "downward" (from lower row)
        int pawnDir = (attackerColor == PieceColor.WHITE) ? 1 : -1;
        for (int dc : new int[]{-1, 1}) {
            int pr = r + pawnDir, pc = c + dc;
            if (isOnBoard(pr, pc)) {
                Piece p = grid[pr][pc];
                if (p != null && p.getType() == PieceType.PAWN && p.getColor() == attackerColor) return true;
            }
        }

        // Knight attacks
        int[][] knightOff = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        for (int[] off : knightOff) {
            int nr = r + off[0], nc = c + off[1];
            if (isOnBoard(nr, nc)) {
                Piece p = grid[nr][nc];
                if (p != null && p.getType() == PieceType.KNIGHT && p.getColor() == attackerColor) return true;
            }
        }

        // Sliding piece attacks (rook/queen for straights; bishop/queen for diagonals)
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int i = 0; i < 8; i++) {
            boolean diagonal = (dirs[i][0] != 0 && dirs[i][1] != 0);
            for (int dist = 1; dist < 8; dist++) {
                int nr = r + dirs[i][0] * dist, nc = c + dirs[i][1] * dist;
                if (!isOnBoard(nr, nc)) break;
                Piece p = grid[nr][nc];
                if (p != null) {
                    if (p.getColor() == attackerColor) {
                        if (diagonal && (p.getType() == PieceType.BISHOP || p.getType() == PieceType.QUEEN)) return true;
                        if (!diagonal && (p.getType() == PieceType.ROOK || p.getType() == PieceType.QUEEN)) return true;
                    }
                    break; // blocked
                }
            }
        }

        // King attacks (one square in any direction)
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (isOnBoard(nr, nc)) {
                    Piece p = grid[nr][nc];
                    if (p != null && p.getType() == PieceType.KING && p.getColor() == attackerColor) return true;
                }
            }
        }

        return false;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /** Deep copies this board (used for display; not needed for AI search since makeMove/undoMove are reversible). */
    public Board deepCopy() {
        Board copy = new Board(true);
        copy.currentPlayer    = this.currentPlayer;
        copy.enPassantTarget  = this.enPassantTarget;
        copy.castlingRights   = this.castlingRights.clone();
        copy.whiteKingPos     = this.whiteKingPos;
        copy.blackKingPos     = this.blackKingPos;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] != null) copy.grid[r][c] = grid[r][c].copy();
            }
        }
        return copy;
    }

    /** Clears the board (used when loading a saved game). */
    public void clear() {
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) grid[r][c] = null;
        enPassantTarget = null;
        castlingRights  = new boolean[]{true, true, true, true};
        currentPlayer   = PieceColor.WHITE;
        whiteKingPos    = null;
        blackKingPos    = null;
    }

    /** Returns a count of all pieces for the given color. */
    public int countPieces(PieceColor color) {
        int count = 0;
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (grid[r][c] != null && grid[r][c].getColor() == color) count++;
        return count;
    }

    private boolean isOnBoard(int r, int c) { return r >= 0 && r < 8 && c >= 0 && c < 8; }

    private void revokeCastlingRight(Position pos) {
        if      (pos.equals(new Position(7, 7))) castlingRights[WK] = false;
        else if (pos.equals(new Position(7, 0))) castlingRights[WQ] = false;
        else if (pos.equals(new Position(0, 7))) castlingRights[BK] = false;
        else if (pos.equals(new Position(0, 0))) castlingRights[BQ] = false;
    }

    private Piece createPiece(PieceType type, PieceColor color) {
        return switch (type) {
            case QUEEN  -> new Queen(color);
            case ROOK   -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default     -> new Queen(color);
        };
    }
}
