package com.cleancode.tetris;

import java.util.List;

public class TetrominoConfig {
    private String name;
    private List<RotationConfig> rotations;

    public String getName() {
        return name;
    }

    public List<RotationConfig> getRotations() {
        return rotations;
    }
}