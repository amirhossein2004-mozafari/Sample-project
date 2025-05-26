package com.cleancode.tetris;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ConfigurationManager {
    private static ConfigurationManager instance;

    private int rows;
    private int cols;
    private int gameSpeed;
    private int initialShapeX;
    private int initialShapeY;
    private int cellSize;
    private Map<String, List<RotationConfig>> tetrominoShapes;
    private List<String> shapeNames;

    private static class ConfigData {
        int rows;
        int cols;
        int gameSpeed;
        int initialShapeX;
        int initialShapeY;
        int cellSize;
        List<TetrominoConfig> tetrominoes;
    }

    private ConfigurationManager() {
        loadConfig();
    }

    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    private void loadConfig() {
        Gson gson = new Gson();
        String configFilePath = "/config.json";
        try (InputStream is = ConfigurationManager.class.getResourceAsStream(configFilePath);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            if (is == null) {
                setDefaultValuesOnError();
                return;
            }

            ConfigData fullConfig = gson.fromJson(reader, ConfigData.class);

            this.rows = fullConfig.rows;
            this.cols = fullConfig.cols;
            this.gameSpeed = fullConfig.gameSpeed;
            this.initialShapeX = fullConfig.initialShapeX;
            this.initialShapeY = fullConfig.initialShapeY;
            this.cellSize = fullConfig.cellSize;

            this.tetrominoShapes = new HashMap<>();
            List<String> tempShapeNames = new ArrayList<>();
            if (fullConfig.tetrominoes != null) {
                for (TetrominoConfig tc : fullConfig.tetrominoes) {
                    this.tetrominoShapes.put(tc.getName(), tc.getRotations());
                    tempShapeNames.add(tc.getName());
                }
            }
            this.shapeNames = Collections.unmodifiableList(tempShapeNames);

        } catch (Exception e) {
            setDefaultValuesOnError();
        }
    }

    private void setDefaultValuesOnError() {
        this.rows = 20;
        this.cols = 10;
        this.gameSpeed = 500;
        this.initialShapeX = 4;
        this.initialShapeY = 0;
        this.cellSize = 20;
        this.tetrominoShapes = new HashMap<>();
        this.shapeNames = Collections.emptyList();
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getGameSpeed() { return gameSpeed; }
    public int getInitialShapeX() { return initialShapeX; }
    public int getInitialShapeY() { return initialShapeY; }
    public int getCellSize() { return cellSize; }
    public List<String> getShapeNames() { return shapeNames; }

    public int[] getShapeXCoordinates(String shapeName, int angle) {
        List<RotationConfig> rotations = tetrominoShapes.get(shapeName);
        if (rotations != null && angle >= 0 && angle < rotations.size()) {
            return rotations.get(angle).getXCoords();
        }
        return new int[]{0,0,0,0};
    }

    public int[] getShapeYCoordinates(String shapeName, int angle) {
        List<RotationConfig> rotations = tetrominoShapes.get(shapeName);
        if (rotations != null && angle >= 0 && angle < rotations.size()) {
            return rotations.get(angle).getYCoords();
        }
        return new int[]{0,0,0,0};
    }
}