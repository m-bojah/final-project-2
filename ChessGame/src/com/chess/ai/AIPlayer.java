package com.chess.ai;

import com.chess.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AIPlayer {

    private static final int HARD_DEPTH   = 4;
    private static final int MEDIUM_DEPTH = 2;
    private static final int MATE_SCORE   = 100_000;

    private final Difficulty     difficulty;
    private final BoardEvaluator evaluator;
    private final Random         random;

    public AIPlayer(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.evaluator  = new BoardEvaluator();
        this.random     = new Random();
    }

    public Difficulty getDifficulty() { return difficulty; }

    /** Returns the best move for the current player on the given board. */
    public Move getBestMove(Board board) {
        List<Move> legal = board.getLegalMoves();
        if (legal.isEmpty()) return null;

        return switch (difficulty) {
            case EASY   -> getEasyMove(legal);
            case MEDIUM -> getSearchMove(board, legal, MEDIUM_DEPTH);
            case HARD   -> getSearchMove(board, legal, HARD_DEPTH);
        };
    }

    // ── Easy ──────────────────────────────────────────────────────────────────
    private Move getEasyMove(List<Move> legal) {
        return legal.get(random.nextInt(legal.size()));
    }

    // ── Medium / Hard via negamax + alpha-beta ────────────────────────────────
    private Move getSearchMove(Board board, List<Move> legal, int depth) {
        List<Move> ordered = orderMoves(legal, board);
        Move bestMove      = ordered.get(0);
        int  bestScore     = Integer.MIN_VALUE + 1;
        int  alpha         = Integer.MIN_VALUE + 1;
        int  beta          = Integer.MAX_VALUE;

        for (Move move : ordered) {
            board.makeMove(move);
            int score = -negamax(board, depth - 1, -beta, -alpha);
            board.undoMove(move);

            if (score > bestScore) {
                bestScore = score;
                bestMove  = move;
            }
            if (score > alpha) alpha = score;
        }
        return bestMove;
    }

    private int negamax(Board board, int depth, int alpha, int beta) {
        GameState state = board.getGameState();

        if (state == GameState.CHECKMATE) return -(MATE_SCORE + depth); // faster mates score higher
        if (state == GameState.STALEMATE) return 0;
        if (depth == 0)                   return evaluator.evaluate(board);

        List<Move> moves = orderMoves(board.getLegalMoves(), board);
        int maxScore = Integer.MIN_VALUE + 1;

        for (Move move : moves) {
            board.makeMove(move);
            int score = -negamax(board, depth - 1, -beta, -alpha);
            board.undoMove(move);

            if (score > maxScore) maxScore = score;
            if (score > alpha)    alpha    = score;
            if (alpha >= beta)    break; // alpha-beta cutoff
        }
        return maxScore;
    }

    /**
     * Orders moves to improve alpha-beta pruning efficiency:
     * captures first, then checks, then quiet moves.
     */
    private List<Move> orderMoves(List<Move> moves, Board board) {
        List<Move> result = new ArrayList<>(moves);
        result.sort((a, b) -> Integer.compare(moveScore(b, board), moveScore(a, board)));
        return result;
    }

    private int moveScore(Move move, Board board) {
        int score = 0;
        if (move.isCapture()) {
            Piece captured = board.getPiece(move.getTo());
            Piece mover    = board.getPiece(move.getFrom());
            if (captured != null && mover != null) {
                // MVV-LVA: prefer high-value captures with low-value pieces
                score += 10 * captured.getType().getValue() - mover.getType().getValue();
            }
        }
        if (move.isPromotion()) score += 800;
        return score;
    }
}
