package com.chess.persistence;

import com.chess.ai.Difficulty;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StatisticsManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STATS_FILE = "chess_stats.dat";

    private int gamesPlayed;
    private int wins;
    private int losses;
    private int draws;
    private Map<Difficulty, int[]> difficultyStats; // [wins, losses, draws]

    public StatisticsManager() {
        difficultyStats = new HashMap<>();
        for (Difficulty d : Difficulty.values()) {
            difficultyStats.put(d, new int[3]);
        }
    }

    public static StatisticsManager load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STATS_FILE))) {
            return (StatisticsManager) ois.readObject();
        } catch (Exception e) {
            return new StatisticsManager();
        }
    }

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATS_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Could not save statistics: " + e.getMessage());
        }
    }

    public void recordWin(Difficulty d) {
        gamesPlayed++; wins++;
        if (d != null) difficultyStats.get(d)[0]++;
        save();
    }

    public void recordLoss(Difficulty d) {
        gamesPlayed++; losses++;
        if (d != null) difficultyStats.get(d)[1]++;
        save();
    }

    public void recordDraw(Difficulty d) {
        gamesPlayed++; draws++;
        if (d != null) difficultyStats.get(d)[2]++;
        save();
    }

    public int getGamesPlayed() { return gamesPlayed; }
    public int getWins()        { return wins; }
    public int getLosses()      { return losses; }
    public int getDraws()       { return draws; }

    public double getWinPercentage() {
        return gamesPlayed == 0 ? 0.0 : (double) wins / gamesPlayed * 100.0;
    }

    public int[] getDifficultyStats(Difficulty d) {
        return difficultyStats.getOrDefault(d, new int[3]);
    }

    public String getSummary() {
        return String.format("Played: %d | Wins: %d | Losses: %d | Draws: %d | Win%%: %.1f%%",
                gamesPlayed, wins, losses, draws, getWinPercentage());
    }
}
