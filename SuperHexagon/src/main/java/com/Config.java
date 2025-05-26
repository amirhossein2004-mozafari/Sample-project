package com;

import javafx.scene.paint.Color;

public class Config {
    public static final double BACKGROUND_HUE_CHANGE_SPEED = 0.1;
    public static final double BACKGROUND_SATURATION = 0.8;
    public static final double BACKGROUND_BRIGHTNESS = 0.6;

    public static final double BACKGROUND_ALT_BRIGHTNESS_FACTOR = 1.15;
    public static final double WALL_PATTERN_ALTERNATING_PROBABILITY = 0.4;

    public static final double WIDTH = 800;
    public static final double HEIGHT = 700;
    public static final double CENTER_X = WIDTH / 2;
    public static final double CENTER_Y = HEIGHT / 2;
    public static final String BACKGROUND_COLOR = "#222";

    // Center Hexagon
    public static final double CENTER_HEX_RADIUS = 40;
    public static final Color CENTER_HEX_COLOR = Color.DARKGRAY;
    public static final double CENTER_HEX_STROKE_WIDTH = 2;

    // Player
    public static final double PLAYER_RADIUS_FROM_CENTER = CENTER_HEX_RADIUS + 15;
    public static final double PLAYER_SIZE = 10;
    public static final double PLAYER_ROTATION_SPEED_DEG_PER_FRAME = 3.0;
    public static final Color PLAYER_COLOR = Color.CYAN;

    // Walls
    public static final double INITIAL_WALL_SPEED_PIXELS_PER_FRAME = 1.5;
    public static final double WALL_THICKNESS = 15;
    public static final double WALL_START_DISTANCE = WIDTH / 2 + 50;
    public static final int SIDES = 6;
    public static final long WALL_SPAWN_INTERVAL_NANOS = 1_500_000_000L;
    public static final Color WALL_COLOR = Color.ORANGERED;
    public static final double SPEED_INCREASE_FACTOR_PER_SECOND = 0.07;

    // UI
    public static final Color SCORE_TEXT_COLOR = Color.WHITE;
    public static final int SCORE_TEXT_FONT_SIZE = 20;
    public static final Color GAME_OVER_TEXT_COLOR = Color.RED;
    public static final int GAME_OVER_FONT_SIZE = 40;


    public static final double WORLD_ROTATION_SPEED_DEG_PER_FRAME = 0.5;
}