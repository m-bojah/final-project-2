package com.chess.view;

import com.chess.ai.Difficulty;
import com.chess.controller.GameManager;
import com.chess.model.GameState;
import com.chess.model.PieceColor;
import com.chess.persistence.StatisticsManager;
import com.chess.utils.SoundManager;
import com.chess.utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Main application window. Wires together all panels and the game controller.
 */
public class ChessGUI extends JFrame implements GameManager.GameListener {

    private GameManager    game;
    private BoardPanel     boardPanel;
    private StatusPanel    statusPanel;
    private MoveHistoryPanel historyPanel;
    private TimerPanel     timerPanel;
    private JButton        undoBtn;
    private JButton        redoBtn;

    public ChessGUI() {
        super("Java Chess");
        game = new GameManager(this);
        game.addListener(this);
        buildUI();
        applyTheme();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildUI() {
        setJMenuBar(buildMenuBar());

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        root.setBackground(ThemeManager.getPanelBackground());

        boardPanel   = new BoardPanel(game);
        statusPanel  = new StatusPanel(game);
        historyPanel = new MoveHistoryPanel(game);
        timerPanel   = new TimerPanel(game);

        root.add(boardPanel, BorderLayout.CENTER);
        root.add(buildSidePanel(), BorderLayout.EAST);

        add(root);
        pack();
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(ThemeManager.getPanelBackground());
        side.setPreferredSize(new Dimension(200, boardPanel.getPreferredSize().height));

        side.add(timerPanel);
        side.add(Box.createVerticalStrut(6));
        side.add(statusPanel);
        side.add(Box.createVerticalStrut(6));
        side.add(historyPanel);
        side.add(Box.createVerticalStrut(6));
        side.add(buildButtonPanel());
        return side;
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.setBackground(ThemeManager.getPanelBackground());

        undoBtn = button("Undo", e -> { game.undo(); historyPanel.rebuild(); });
        redoBtn = button("Redo", e -> { game.redo(); historyPanel.rebuild(); });
        JButton saveBtn = button("Save",  e -> game.saveGame());
        JButton loadBtn = button("Load",  e -> { game.loadGame(); historyPanel.rebuild(); refresh(); });
        JButton statsBtn = button("Stats", e -> showStats());
        JButton helpBtn  = button("Help",  e -> showHelp());

        p.add(undoBtn); p.add(redoBtn);
        p.add(saveBtn); p.add(loadBtn);
        p.add(statsBtn); p.add(helpBtn);
        return p;
    }

    private JButton button(String label, java.awt.event.ActionListener action) {
        JButton b = new JButton(label);
        b.addActionListener(action);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return b;
    }

    // ── Menu bar ──────────────────────────────────────────────────────────────

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        JMenuItem newEasy   = new JMenuItem("New Game – Easy");
        JMenuItem newMedium = new JMenuItem("New Game – Medium");
        JMenuItem newHard   = new JMenuItem("New Game – Hard");
        JMenuItem newCustom = new JMenuItem("New Game – Custom Time…");
        JMenuItem exitItem  = new JMenuItem("Exit");

        newEasy.addActionListener   (e -> startNew(Difficulty.EASY,   600));
        newMedium.addActionListener (e -> startNew(Difficulty.MEDIUM, 600));
        newHard.addActionListener   (e -> startNew(Difficulty.HARD,   600));
        newCustom.addActionListener (e -> startNewCustom());
        exitItem.addActionListener  (e -> System.exit(0));

        gameMenu.add(newEasy); gameMenu.add(newMedium); gameMenu.add(newHard);
        gameMenu.add(newCustom); gameMenu.addSeparator(); gameMenu.add(exitItem);
        bar.add(gameMenu);

        // Options menu
        JMenu optMenu = new JMenu("Options");
        JMenuItem lightTheme = new JMenuItem("Light Theme");
        JMenuItem darkTheme  = new JMenuItem("Dark Theme");
        JCheckBoxMenuItem soundItem = new JCheckBoxMenuItem("Sound Effects", true);

        lightTheme.addActionListener(e -> { ThemeManager.setTheme(ThemeManager.Theme.LIGHT); applyTheme(); });
        darkTheme.addActionListener (e -> { ThemeManager.setTheme(ThemeManager.Theme.DARK);  applyTheme(); });
        soundItem.addActionListener (e -> SoundManager.setEnabled(soundItem.isSelected()));

        optMenu.add(lightTheme); optMenu.add(darkTheme);
        optMenu.addSeparator(); optMenu.add(soundItem);
        bar.add(optMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem rulesItem = new JMenuItem("Chess Rules");
        JMenuItem aboutItem = new JMenuItem("About");
        rulesItem.addActionListener(e -> showHelp());
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Java Chess Game\nBuilt with Java Swing + Minimax AI", "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(rulesItem); helpMenu.add(aboutItem);
        bar.add(helpMenu);

        return bar;
    }

    // ── Game control helpers ──────────────────────────────────────────────────

    private void startNew(Difficulty diff, int secs) {
        game.startNewGame(diff, secs);
        historyPanel.clear();
        refresh();
    }

    private void startNewCustom() {
        String input = JOptionPane.showInputDialog(this,
                "Enter time per player in minutes (1–60):", "Custom Time", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            int mins = Integer.parseInt(input.trim());
            if (mins < 1 || mins > 60) throw new NumberFormatException();
            Object[] opts = {"Easy", "Medium", "Hard"};
            int ch = JOptionPane.showOptionDialog(this, "Select difficulty:", "Difficulty",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts, opts[1]);
            Difficulty diff = switch (ch) { case 0 -> Difficulty.EASY; case 2 -> Difficulty.HARD; default -> Difficulty.MEDIUM; };
            startNew(diff, mins * 60);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a number between 1 and 60.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        statusPanel.refresh();
        timerPanel.update(game);
        boardPanel.clearSelection();
        boardPanel.repaint();
        undoBtn.setEnabled(game.canUndo());
        redoBtn.setEnabled(game.canRedo());
    }

    private void applyTheme() {
        Color bg = ThemeManager.getPanelBackground();
        getContentPane().setBackground(bg);
        repaint();
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showStats() {
        StatisticsManager s = game.getStats();
        String msg = String.format(
                "Games Played: %d%nWins: %d%nLosses: %d%nDraws: %d%nWin Rate: %.1f%%%n%n" +
                "Easy:   W%d L%d D%d%nMedium: W%d L%d D%d%nHard:   W%d L%d D%d",
                s.getGamesPlayed(), s.getWins(), s.getLosses(), s.getDraws(), s.getWinPercentage(),
                s.getDifficultyStats(Difficulty.EASY)[0],   s.getDifficultyStats(Difficulty.EASY)[1],   s.getDifficultyStats(Difficulty.EASY)[2],
                s.getDifficultyStats(Difficulty.MEDIUM)[0], s.getDifficultyStats(Difficulty.MEDIUM)[1], s.getDifficultyStats(Difficulty.MEDIUM)[2],
                s.getDifficultyStats(Difficulty.HARD)[0],   s.getDifficultyStats(Difficulty.HARD)[1],   s.getDifficultyStats(Difficulty.HARD)[2]);
        JOptionPane.showMessageDialog(this, msg, "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelp() {
        String rules = """
                CHESS RULES SUMMARY
                ─────────────────────────────────────
                • Click a piece, then click the destination.
                • Green dot = legal move   |   Ring = capture
                • Pawns promote when reaching the back rank.
                • Castling: King + Rook both unmoved, no pieces between,
                  king not in check before/during/after.
                • En passant: capture a pawn that just double-pushed.
                • Check: your king is under attack — you must escape.
                • Checkmate: king in check with no escape → game over.
                • Stalemate: no legal moves but not in check → draw.

                CONTROLS
                ─────────────────────────────────────
                • Undo: takes back your last move + AI response.
                • Redo: replays an undone move.
                • Save/Load: preserves or restores the full game state.
                • Menu: New Game selects difficulty and optionally time.
                """;
        JTextArea ta = new JTextArea(rules);
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(420, 320));
        JOptionPane.showMessageDialog(this, sp, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── GameListener ──────────────────────────────────────────────────────────

    @Override public void onBoardChanged()              { SwingUtilities.invokeLater(() -> { boardPanel.repaint(); refresh(); }); }
    @Override public void onGameStateChanged(GameState s){ SwingUtilities.invokeLater(() -> statusPanel.refresh()); }
    @Override public void onPlayerChanged(PieceColor p) { SwingUtilities.invokeLater(() -> { statusPanel.refresh(); timerPanel.update(game); }); }
    @Override public void onTimerTick(PieceColor p, int s){ SwingUtilities.invokeLater(() -> timerPanel.update(game)); }
    @Override public void onMoveRecorded(String n)      { SwingUtilities.invokeLater(() -> { historyPanel.addMove(n); undoBtn.setEnabled(game.canUndo()); redoBtn.setEnabled(game.canRedo()); }); }
    @Override public void onAIThinking(boolean b)       { SwingUtilities.invokeLater(() -> boardPanel.setAIThinking(b)); }
}
