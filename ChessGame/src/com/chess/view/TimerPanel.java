package com.chess.view;

import com.chess.controller.GameManager;
import com.chess.model.PieceColor;
import com.chess.utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TimerPanel extends JPanel {

    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;
    private JLabel whiteActiveIndicator;
    private JLabel blackActiveIndicator;

    public TimerPanel(GameManager game) {
        setLayout(new GridLayout(2, 1, 4, 4));
        setBackground(ThemeManager.getPanelBackground());
        setBorder(new EmptyBorder(5, 5, 5, 5));

        whiteActiveIndicator = indicator();
        blackActiveIndicator = indicator();
        whiteTimerLabel = timerLabel("White (You): " + game.getWhitePlayer().getFormattedTime());
        blackTimerLabel = timerLabel("Black  (AI): " + game.getBlackPlayer().getFormattedTime());

        JPanel whiteRow = row(whiteActiveIndicator, whiteTimerLabel);
        JPanel blackRow = row(blackActiveIndicator, blackTimerLabel);
        add(blackRow); // black's clock on top (opponent)
        add(whiteRow); // white's clock on bottom (player)
    }

    private JLabel timerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 14));
        l.setForeground(ThemeManager.getTextColor());
        return l;
    }

    private JLabel indicator() {
        JLabel l = new JLabel("●");
        l.setFont(new Font("SansSerif", Font.PLAIN, 10));
        l.setForeground(Color.LIGHT_GRAY);
        return l;
    }

    private JPanel row(JLabel indicator, JLabel timer) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(ThemeManager.getPanelBackground());
        p.add(indicator); p.add(timer);
        return p;
    }

    public void update(GameManager game) {
        whiteTimerLabel.setText("White (You): " + game.getWhitePlayer().getFormattedTime());
        blackTimerLabel.setText("Black  (AI): " + game.getBlackPlayer().getFormattedTime());

        PieceColor current = game.getCurrentPlayer();
        boolean wActive = (current == PieceColor.WHITE && !game.isGameOver());
        boolean bActive = (current == PieceColor.BLACK && !game.isGameOver());

        whiteActiveIndicator.setForeground(wActive ? new Color(0, 200, 0) : Color.LIGHT_GRAY);
        blackActiveIndicator.setForeground(bActive ? new Color(0, 200, 0) : Color.LIGHT_GRAY);

        // Warn when time is low (< 30 seconds)
        if (game.getWhitePlayer().getTimeSeconds() < 30)
            whiteTimerLabel.setForeground(Color.RED);
        else
            whiteTimerLabel.setForeground(ThemeManager.getTextColor());

        if (game.getBlackPlayer().getTimeSeconds() < 30)
            blackTimerLabel.setForeground(Color.RED);
        else
            blackTimerLabel.setForeground(ThemeManager.getTextColor());
    }
}
