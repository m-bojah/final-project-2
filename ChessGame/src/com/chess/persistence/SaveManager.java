package com.chess.persistence;

import com.chess.model.Board;
import com.chess.model.Move;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.List;

public class SaveManager {

    private static final String DEFAULT_EXTENSION = "chess";

    /** Encapsulates everything needed to restore a game. */
    public static class GameSave implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Board      board;
        public final List<Move> history;
        public final int        whiteSecs;
        public final int        blackSecs;
        public final String     difficulty;

        public GameSave(Board board, List<Move> history, int whiteSecs, int blackSecs, String difficulty) {
            this.board      = board;
            this.history    = history;
            this.whiteSecs  = whiteSecs;
            this.blackSecs  = blackSecs;
            this.difficulty = difficulty;
        }
    }

    /** Shows a save-file dialog and writes the game. Returns true on success. */
    public static boolean saveGame(JFrame parent, GameSave save) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Game");
        fc.setFileFilter(new FileNameExtensionFilter("Chess Save Files (*." + DEFAULT_EXTENSION + ")", DEFAULT_EXTENSION));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return false;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith("." + DEFAULT_EXTENSION)) {
            file = new File(file.getAbsolutePath() + "." + DEFAULT_EXTENSION);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(save);
            JOptionPane.showMessageDialog(parent, "Game saved successfully.", "Save Game", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Failed to save: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** Shows a load-file dialog and reads a saved game. Returns null on failure/cancel. */
    public static GameSave loadGame(JFrame parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load Game");
        fc.setFileFilter(new FileNameExtensionFilter("Chess Save Files (*." + DEFAULT_EXTENSION + ")", DEFAULT_EXTENSION));
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fc.getSelectedFile()))) {
            return (GameSave) ois.readObject();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Failed to load: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
