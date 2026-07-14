package com.chess.view;

import com.chess.controller.GameManager;
import com.chess.model.*;
import com.chess.utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusPanel extends JPanel {

    private final GameManager game;

    private JLabel turnLabel;
    private JLabel difficultyLabel;
    private JLabel stateLabel;
    private JLabel moveCountLabel;
    private JLabel statsLabel;
    private JLabel capturedWhiteLabel;
    private JLabel capturedBlackLabel;

    public StatusPanel(GameManager game) {
        this.game = game;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ThemeManager.getPanelBackground());
        buildUI();
    }

    private void buildUI() {
        add(section("Game Info"));

        turnLabel       = info("Turn: White");
        difficultyLabel = info("Difficulty: Medium");
        stateLabel      = info("Status: Playing");
        moveCountLabel  = info("Moves: 0");

        add(turnLabel); add(difficultyLabel); add(stateLabel); add(moveCountLabel);
        add(Box.createVerticalStrut(12));

        add(section("Captured Pieces"));
        capturedWhiteLabel = info("White lost: —");
        capturedBlackLabel = info("Black lost: —");
        add(capturedWhiteLabel);
        add(capturedBlackLabel);
        add(Box.createVerticalStrut(12));

        add(section("Statistics"));
        statsLabel = info(game.getStats().getSummary());
        add(statsLabel);
    }

    private JLabel section(String title) {
        JLabel l = new JLabel(title);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(new Color(100, 100, 200));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel info(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(ThemeManager.getTextColor());
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    public void refresh() {
        PieceColor current = game.getCurrentPlayer();
        turnLabel.setText("Turn: " + (current == PieceColor.WHITE ? "White (You)" : "Black (AI)"));
        difficultyLabel.setText("Difficulty: " + game.getDifficulty().getDisplayName());

        GameState state = game.getGameState();
        String stateText = switch (state) {
            case CHECK      -> "Check!";
            case CHECKMATE  -> "Checkmate!";
            case STALEMATE  -> "Stalemate (Draw)";
            default         -> "Playing";
        };
        stateLabel.setForeground(state == GameState.CHECK || state == GameState.CHECKMATE
                ? Color.RED : ThemeManager.getTextColor());
        stateLabel.setText("Status: " + stateText);
        moveCountLabel.setText("Moves: " + game.getMoveCount());

        // Captured pieces (simple material count difference)
        updateCapturedLabels();
        statsLabel.setText(game.getStats().getSummary());
        revalidate(); repaint();
    }

    private void updateCapturedLabels() {
        Board board = game.getBoard();
        int[] whiteMaterial = new int[6];
        int[] blackMaterial = new int[6];
        int[] startingPieces = {1, 1, 2, 2, 2, 8}; // K, Q, R, B, Kn, P for each side

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                if (p == null) continue;
                int idx = switch (p.getType()) {
                    case KING -> 0; case QUEEN -> 1; case ROOK -> 2;
                    case BISHOP -> 3; case KNIGHT -> 4; case PAWN -> 5;
                };
                if (p.getColor() == PieceColor.WHITE) whiteMaterial[idx]++;
                else                                  blackMaterial[idx]++;
            }
        }
        // Captured by black (white pieces missing)
        capturedWhiteLabel.setText("White lost: " + buildCapturedString(whiteMaterial, PieceColor.WHITE));
        capturedBlackLabel.setText("Black lost: " + buildCapturedString(blackMaterial, PieceColor.BLACK));
    }

    private String buildCapturedString(int[] onBoard, PieceColor color) {
        int[] start = {1, 1, 2, 2, 2, 8};
        char[] wSymbols = {'♔','♕','♖','♗','♘','♙'};
        char[] bSymbols = {'♚','♛','♜','♝','♞','♟'};
        char[] symbols  = (color == PieceColor.WHITE) ? wSymbols : bSymbols;

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 6; i++) { // skip king
            int captured = start[i] - onBoard[i];
            for (int k = 0; k < captured; k++) sb.append(symbols[i]);
        }
        return sb.length() == 0 ? "—" : sb.toString();
    }
}
