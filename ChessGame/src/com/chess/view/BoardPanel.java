package com.chess.view;

import com.chess.controller.GameManager;
import com.chess.model.*;
import com.chess.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Renders the 8×8 chess board and handles mouse-click move input.
 */
public class BoardPanel extends JPanel {

    private static final int SQUARE_SIZE = 70;
    private static final int BORDER      = 30; // for coordinate labels

    private final GameManager game;
    private Position selected;     // currently selected piece square
    private List<Move> legalMoves; // legal moves for the selected piece
    private boolean aiThinking = false;

    public BoardPanel(GameManager game) {
        this.game = game;
        int size = SQUARE_SIZE * 8 + BORDER * 2;
        setPreferredSize(new Dimension(size, size));
        setBackground(ThemeManager.getPanelBackground());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setAIThinking(boolean thinking) {
        this.aiThinking = thinking;
        repaint();
    }

    public void clearSelection() {
        selected   = null;
        legalMoves = null;
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawCoordinates(g2);
        drawSquares(g2);
        drawHighlights(g2);
        drawPieces(g2);
        if (aiThinking) drawThinkingOverlay(g2);

        g2.dispose();
    }

    private void drawCoordinates(Graphics2D g2) {
        g2.setColor(ThemeManager.getTextColor());
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < 8; i++) {
            // Files (a–h) along bottom
            String file = String.valueOf((char) ('a' + i));
            int x = BORDER + i * SQUARE_SIZE + (SQUARE_SIZE - fm.stringWidth(file)) / 2;
            g2.drawString(file, x, BORDER + 8 * SQUARE_SIZE + 18);

            // Ranks (8–1) along left
            String rank = String.valueOf(8 - i);
            int y = BORDER + i * SQUARE_SIZE + (SQUARE_SIZE + fm.getAscent()) / 2;
            g2.drawString(rank, (BORDER - fm.stringWidth(rank)) / 2, y);
        }
    }

    private void drawSquares(Graphics2D g2) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                g2.setColor(light ? ThemeManager.getLightSquareColor() : ThemeManager.getDarkSquareColor());
                g2.fillRect(BORDER + c * SQUARE_SIZE, BORDER + r * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void drawHighlights(Graphics2D g2) {
        Move lastMove = game.getLastMove();
        // Last-move highlight
        if (lastMove != null) {
            highlightSquare(g2, lastMove.getFrom(), ThemeManager.getLastMoveHighlight());
            highlightSquare(g2, lastMove.getTo(),   ThemeManager.getLastMoveHighlight());
        }
        // Check highlight
        if (game.getGameState() == GameState.CHECK || game.getGameState() == GameState.CHECKMATE) {
            Position kingPos = game.getBoard().getCurrentPlayer() == PieceColor.WHITE
                    ? game.getBoard().getWhiteKingPos() : game.getBoard().getBlackKingPos();
            if (kingPos != null) highlightSquare(g2, kingPos, ThemeManager.getCheckHighlight());
        }
        // Selected piece highlight
        if (selected != null) {
            highlightSquare(g2, selected, ThemeManager.getSelectHighlight());
        }
        // Legal move dots
        if (legalMoves != null) {
            for (Move m : legalMoves) {
                Position to = m.getTo();
                Piece target = game.getBoard().getPiece(to);
                if (target != null && target.getColor() != game.getBoard().getCurrentPlayer()) {
                    // Capture: ring highlight
                    highlightSquare(g2, to, ThemeManager.getMoveHighlight());
                } else {
                    // Quiet: dot
                    drawMoveDot(g2, to);
                }
            }
        }
    }

    private void highlightSquare(Graphics2D g2, Position pos, Color color) {
        int x = BORDER + pos.getCol() * SQUARE_SIZE;
        int y = BORDER + pos.getRow() * SQUARE_SIZE;
        g2.setColor(color);
        g2.fillRect(x, y, SQUARE_SIZE, SQUARE_SIZE);
    }

    private void drawMoveDot(Graphics2D g2, Position pos) {
        int x = BORDER + pos.getCol() * SQUARE_SIZE + SQUARE_SIZE / 2;
        int y = BORDER + pos.getRow() * SQUARE_SIZE + SQUARE_SIZE / 2;
        int r = SQUARE_SIZE / 5;
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(x - r, y - r, r * 2, r * 2);
    }

    private void drawPieces(Graphics2D g2) {
        Board board = game.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPiece(new Position(r, c));
                if (piece == null) continue;
                int x = BORDER + c * SQUARE_SIZE;
                int y = BORDER + r * SQUARE_SIZE;
                PieceRenderer.draw(g2, piece.getType(), piece.getColor(), x, y, SQUARE_SIZE);
            }
        }
    }

    private void drawThinkingOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRect(BORDER, BORDER, SQUARE_SIZE * 8, SQUARE_SIZE * 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        String msg = "AI is thinking...";
        FontMetrics fm = g2.getFontMetrics();
        int x = BORDER + (SQUARE_SIZE * 8 - fm.stringWidth(msg)) / 2;
        int y = BORDER + SQUARE_SIZE * 4;
        g2.drawString(msg, x, y);
    }

    // ── Input ────────────────────────────────────────────────────────────────

    private void handleClick(int px, int py) {
        if (aiThinking || game.isGameOver()) return;
        if (game.getBoard().getCurrentPlayer() != PieceColor.WHITE) return; // only human (white)

        int col = (px - BORDER) / SQUARE_SIZE;
        int row = (py - BORDER) / SQUARE_SIZE;
        if (col < 0 || col >= 8 || row < 0 || row >= 8) return;

        Position clicked = new Position(row, col);
        Piece clickedPiece = game.getBoard().getPiece(clicked);

        if (selected == null) {
            // First click: select a piece
            if (clickedPiece != null && clickedPiece.getColor() == PieceColor.WHITE) {
                selected   = clicked;
                legalMoves = game.getBoard().getLegalMovesForPiece(clicked);
                repaint();
            }
        } else {
            // Second click: attempt a move, or re-select
            if (clicked.equals(selected)) {
                clearSelection(); repaint(); return;
            }
            if (clickedPiece != null && clickedPiece.getColor() == PieceColor.WHITE) {
                // Re-select another own piece
                selected   = clicked;
                legalMoves = game.getBoard().getLegalMovesForPiece(clicked);
                repaint();
                return;
            }
            boolean moved = game.humanMove(selected, clicked);
            clearSelection();
            if (!moved) repaint();
        }
    }
}
