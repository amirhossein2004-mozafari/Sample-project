package com;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameRecord {
    private String playerName;
    private double score;
    private String timestamp;

    public GameRecord(String playerName, double score, LocalDateTime dateTime) {
        this.playerName = playerName;
        this.score = score;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.timestamp = dateTime.format(formatter);
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getScore() {
        return score;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s - %s: %.2fs", timestamp, playerName, score);
    }
}