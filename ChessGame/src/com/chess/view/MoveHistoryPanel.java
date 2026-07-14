package com.chess.view;

import com.chess.controller.GameManager;
import com.chess.model.Move;
import com.chess.utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class MoveHistoryPanel extends JPanel {

    private final GameManager game;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> moveList;

    public MoveHistoryPanel(GameManager game) {
        this.game = game;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.getPanelBackground());
        setBorder(new TitledBorder("Move History"));

        moveList = new JList<>(listModel);
        moveList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        moveList.setBackground(ThemeManager.getPanelBackground());
        moveList.setForeground(ThemeManager.getTextColor());

        JScrollPane scroll = new JScrollPane(moveList);
        scroll.setPreferredSize(new Dimension(160, 300));
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(scroll, BorderLayout.CENTER);
    }

    /** Adds a single move notation to the list. */
    public void addMove(String notation) {
        int moveNo = listModel.size() + 1;
        boolean isWhite = (moveNo % 2 == 1);
        if (isWhite) {
            listModel.addElement(String.format("%3d. %s", (moveNo + 1) / 2, notation));
        } else {
            int lastIdx = listModel.size() - 1;
            String prev = listModel.get(lastIdx);
            listModel.set(lastIdx, prev + "  " + notation);
        }
        int last = listModel.size() - 1;
        if (last >= 0) moveList.ensureIndexIsVisible(last);
    }

    /** Rebuilds the list from full history (used after load/undo). */
    public void rebuild() {
        listModel.clear();
        List<Move> history = game.getMoveHistory();
        for (int i = 0; i < history.size(); i++) {
            Move m = history.get(i);
            String notation = m.getAlgebraicNotation() != null ? m.getAlgebraicNotation() : m.toString();
            boolean isWhite = (i % 2 == 0);
            if (isWhite) {
                listModel.addElement(String.format("%3d. %s", i / 2 + 1, notation));
            } else {
                int lastIdx = listModel.size() - 1;
                String prev = listModel.get(lastIdx);
                listModel.set(lastIdx, prev + "  " + notation);
            }
        }
    }

    public void clear() {
        listModel.clear();
    }
}
