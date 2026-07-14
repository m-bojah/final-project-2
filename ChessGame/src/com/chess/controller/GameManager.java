package com.chess.controller;

import com.chess.ai.AIPlayer;
import com.chess.ai.Difficulty;
import com.chess.model.*;
import com.chess.persistence.SaveManager;
import com.chess.persistence.StatisticsManager;
import com.chess.utils.SoundManager;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Orchestrates the chess game: turn management, AI, undo/redo, timers, save/load.
 */
public class GameManager {

    public interface GameListener {
        void onBoardChanged();
        void onGameStateChanged(GameState state);
        void onPlayerChanged(PieceColor player);
        void onTimerTick(PieceColor player, int seconds);
        void onMoveRecorded(String notation);
        void onAIThinking(boolean thinking);
    }

    // Default time per player (10 minutes)
    private static final int DEFAULT_TIME = 600;

    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private AIPlayer aiPlayer;
    private Difficulty difficulty;

    private final Deque<Move>  undoStack = new ArrayDeque<>();
    private final Deque<Move>  redoStack = new ArrayDeque<>();
    private final List<Move>   moveHistory = new ArrayList<>();

    private GameState  currentState = GameState.PLAYING;
    private boolean    gameOver     = false;
    private Move       lastMove;

    private final List<GameListener> listeners = new ArrayList<>();
    private Timer swingTimer;

    private final StatisticsManager stats;
    private JFrame parentFrame;

    public GameManager(JFrame parent) {
        this.parentFrame = parent;
        this.stats = StatisticsManager.load();
        startNewGame(Difficulty.MEDIUM, DEFAULT_TIME);
    }

    // ── Game lifecycle ────────────────────────────────────────────────────────

    public void startNewGame(Difficulty difficulty, int timeSecs) {
        stopTimer();
        this.difficulty   = difficulty;
        this.board        = new Board();
        this.aiPlayer     = new AIPlayer(difficulty);
        this.whitePlayer  = new Player("White (You)", PieceColor.WHITE, true,  timeSecs);
        this.blackPlayer  = new Player("Black (AI)",  PieceColor.BLACK, false, timeSecs);
        undoStack.clear();
        redoStack.clear();
        moveHistory.clear();
        currentState = GameState.PLAYING;
        gameOver     = false;
        lastMove     = null;

        notifyBoardChanged();
        notifyPlayerChanged(PieceColor.WHITE);
        notifyGameStateChanged(GameState.PLAYING);
        startTimer();
    }

    // ── Human move input ──────────────────────────────────────────────────────

    /**
     * Called by the GUI when the human selects a piece and a target square.
     * Handles promotion by showing a dialog.
     */
    public boolean humanMove(Position from, Position to) {
        if (gameOver || !board.getCurrentPlayer().equals(PieceColor.WHITE)) return false;

        List<Move> legal = board.getLegalMovesForPiece(from);
        Move chosen = null;

        // Find a matching legal move (may be a promotion)
        for (Move m : legal) {
            if (m.getTo().equals(to)) {
                if (m.isPromotion()) {
                    chosen = askPromotion(from, to, m.getType());
                    break;
                } else {
                    chosen = m;
                    break;
                }
        }
        }

        if (chosen == null) return false;

        applyMove(chosen);

        // Schedule AI response after a brief delay so the board repaints first
        if (!gameOver && board.getCurrentPlayer() == PieceColor.BLACK) {
            Timer aiDelay = new Timer(150, e -> triggerAIMove());
            aiDelay.setRepeats(false);
            aiDelay.start();
        }
        return true;
    }

    private Move askPromotion(Position from, Position to, Move.MoveType type) {
        Object[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(parentFrame,
                "Choose promotion piece:", "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        PieceType promoType = switch (choice) {
            case 1 -> PieceType.ROOK;
            case 2 -> PieceType.BISHOP;
            case 3 -> PieceType.KNIGHT;
            default -> PieceType.QUEEN;
        };
        return new Move(from, to, type, promoType);
    }

    // ── AI move ───────────────────────────────────────────────────────────────

    private void triggerAIMove() {
        notifyAIThinking(true);
        SwingWorker<Move, Void> worker = new SwingWorker<>() {
            @Override
            protected Move doInBackground() {
                return aiPlayer.getBestMove(board);
            }
            @Override
            protected void done() {
                notifyAIThinking(false);
                try {
                    Move aiMove = get();
                    if (aiMove != null && !gameOver) applyMove(aiMove);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // ── Apply move ────────────────────────────────────────────────────────────

    private void applyMove(Move move) {
        board.makeMove(move);
        undoStack.push(move);
        redoStack.clear();
        moveHistory.add(move);
        lastMove = move;

        buildAlgebraicNotation(move);
        notifyMoveRecorded(move.getAlgebraicNotation() != null ? move.getAlgebraicNotation() : move.toString());

        playSoundForMove(move);

        currentState = board.getGameState();
        notifyGameStateChanged(currentState);
        notifyBoardChanged();
        notifyPlayerChanged(board.getCurrentPlayer());

        if (currentState == GameState.CHECKMATE || currentState == GameState.STALEMATE) {
            handleGameOver();
        }
    }

    private void playSoundForMove(Move move) {
        GameState stateAfter = board.getGameState();
        if (stateAfter == GameState.CHECKMATE) {
            SoundManager.playCheckmate();
        } else if (stateAfter == GameState.CHECK) {
            SoundManager.playCheck();
        } else if (move.isCapture()) {
            SoundManager.playCapture();
        } else {
            SoundManager.playMove();
        }
    }

    private void handleGameOver() {
        gameOver = true;
        stopTimer();
        String msg;
        if (currentState == GameState.CHECKMATE) {
            PieceColor winner = board.getCurrentPlayer().opposite();
            msg = (winner == PieceColor.WHITE ? "White" : "Black") + " wins by checkmate!";
            if (winner == PieceColor.WHITE) stats.recordWin(difficulty);
            else                            stats.recordLoss(difficulty);
        } else {
            msg = "Stalemate — draw!";
            stats.recordDraw(difficulty);
        }
        SwingUtilities.invokeLater(() ->
            JOptionPane.showMessageDialog(parentFrame, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE));
    }

    // ── Undo / Redo ───────────────────────────────────────────────────────────

    public boolean canUndo() { return undoStack.size() >= 2; } // undo both AI and player move

    public void undo() {
        if (!canUndo()) return;
        // Undo AI move then player move
        Move aiMove  = undoStack.pop(); board.undoMove(aiMove);  redoStack.push(aiMove);
        Move humMove = undoStack.pop(); board.undoMove(humMove); redoStack.push(humMove);
        if (!moveHistory.isEmpty()) moveHistory.remove(moveHistory.size() - 1);
        if (!moveHistory.isEmpty()) moveHistory.remove(moveHistory.size() - 1);
        lastMove     = undoStack.isEmpty() ? null : undoStack.peek();
        currentState = board.getGameState();
        gameOver     = false;
        notifyBoardChanged();
        notifyGameStateChanged(currentState);
        notifyPlayerChanged(board.getCurrentPlayer());
    }

    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void redo() {
        if (!canRedo()) return;
        Move humMove = redoStack.pop(); board.makeMove(humMove); undoStack.push(humMove);
        if (!redoStack.isEmpty()) {
            Move aiMove = redoStack.pop(); board.makeMove(aiMove); undoStack.push(aiMove);
        }
        lastMove     = undoStack.isEmpty() ? null : undoStack.peek();
        currentState = board.getGameState();
        notifyBoardChanged();
        notifyGameStateChanged(currentState);
        notifyPlayerChanged(board.getCurrentPlayer());
    }

    // ── Timer ────────────────────────────────────────────────────────────────

    private void startTimer() {
        swingTimer = new Timer(1000, e -> tickTimer());
        swingTimer.start();
    }

    private void stopTimer() {
        if (swingTimer != null) { swingTimer.stop(); swingTimer = null; }
    }

    private void tickTimer() {
        if (gameOver) return;
        PieceColor current = board.getCurrentPlayer();
        Player p = (current == PieceColor.WHITE) ? whitePlayer : blackPlayer;
        p.decrementTime();
        notifyTimerTick(current, p.getTimeSeconds());
        if (p.isOutOfTime()) {
            gameOver = true;
            stopTimer();
            String msg = (current == PieceColor.WHITE ? "White" : "Black") + " ran out of time. "
                       + (current == PieceColor.WHITE ? "Black" : "White") + " wins!";
            if (current == PieceColor.WHITE) stats.recordLoss(difficulty);
            else                             stats.recordWin(difficulty);
            JOptionPane.showMessageDialog(parentFrame, msg, "Time Out", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Save / Load ──────────────────────────────────────────────────────────

    public void saveGame() {
        SaveManager.GameSave save = new SaveManager.GameSave(
                board.deepCopy(), new ArrayList<>(moveHistory),
                whitePlayer.getTimeSeconds(), blackPlayer.getTimeSeconds(),
                difficulty.name());
        SaveManager.saveGame(parentFrame, save);
    }

    public void loadGame() {
        SaveManager.GameSave save = SaveManager.loadGame(parentFrame);
        if (save == null) return;
        stopTimer();
        this.board        = save.board;
        this.difficulty   = Difficulty.valueOf(save.difficulty);
        this.aiPlayer     = new AIPlayer(this.difficulty);
        this.whitePlayer  = new Player("White (You)", PieceColor.WHITE, true,  save.whiteSecs);
        this.blackPlayer  = new Player("Black (AI)",  PieceColor.BLACK, false, save.blackSecs);
        undoStack.clear(); redoStack.clear();
        moveHistory.clear(); moveHistory.addAll(save.history);
        currentState = board.getGameState();
        gameOver     = (currentState == GameState.CHECKMATE || currentState == GameState.STALEMATE);
        lastMove     = moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
        notifyBoardChanged();
        notifyGameStateChanged(currentState);
        notifyPlayerChanged(board.getCurrentPlayer());
        if (!gameOver) startTimer();
    }

    // ── Algebraic notation helper ─────────────────────────────────────────────

    private void buildAlgebraicNotation(Move move) {
        if (move.getAlgebraicNotation() != null) return;
        String s;
        if (move.getType() == Move.MoveType.CASTLING_KINGSIDE)  s = "O-O";
        else if (move.getType() == Move.MoveType.CASTLING_QUEENSIDE) s = "O-O-O";
        else {
            Piece p = move.getMovedPiece();
            String piece = (p != null && p.getType() != PieceType.PAWN)
                    ? String.valueOf(p.getType().name().charAt(0)) : "";
            s = piece + move.getFrom().toString() + (move.isCapture() ? "x" : "") + move.getTo().toString();
            if (move.isPromotion()) s += "=" + move.getPromotionType().name().charAt(0);
        }
        if (board.getGameState() == GameState.CHECKMATE) s += "#";
        else if (board.getGameState() == GameState.CHECK)s += "+";
        move.setAlgebraicNotation(s);
    }

    // ── Listener management ───────────────────────────────────────────────────

    public void addListener(GameListener l)    { listeners.add(l); }
    public void removeListener(GameListener l) { listeners.remove(l); }

    private void notifyBoardChanged()       { listeners.forEach(GameListener::onBoardChanged); }
    private void notifyGameStateChanged(GameState s) { listeners.forEach(l -> l.onGameStateChanged(s)); }
    private void notifyPlayerChanged(PieceColor c)   { listeners.forEach(l -> l.onPlayerChanged(c)); }
    private void notifyTimerTick(PieceColor c, int s){ listeners.forEach(l -> l.onTimerTick(c, s)); }
    private void notifyMoveRecorded(String n)        { listeners.forEach(l -> l.onMoveRecorded(n)); }
    private void notifyAIThinking(boolean b)         { listeners.forEach(l -> l.onAIThinking(b)); }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Board              getBoard()          { return board; }
    public PieceColor         getCurrentPlayer()  { return board.getCurrentPlayer(); }
    public GameState          getGameState()      { return currentState; }
    public Difficulty         getDifficulty()     { return difficulty; }
    public boolean            isGameOver()        { return gameOver; }
    public Move               getLastMove()       { return lastMove; }
    public List<Move>         getMoveHistory()    { return moveHistory; }
    public Player             getWhitePlayer()    { return whitePlayer; }
    public Player             getBlackPlayer()    { return blackPlayer; }
    public StatisticsManager  getStats()          { return stats; }
    public int                getMoveCount()      { return moveHistory.size(); }
}
