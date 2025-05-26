package com;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;



public class Game {

    private final Renderer renderer;
    private final InputHandler inputHandler;
    private final WallSpawner wallSpawner;
    private final Pane gamePane;
    private final SuperHexagonApp mainApp;

    private Player player;
    private List<WallSegment> walls;
    private AnimationTimer gameLoop;

    private boolean gameOver = false;
    private boolean isPaused = false;
    private long startTimeNanos;
    private long pauseStartTimeNanos = 0;
    private double currentWallSpeed;
    private double scoreTimeSeconds;
    private double bestTimeSeconds = 0.0;
    private double worldRotationAngle = 0;
    private Rotate worldRotationTransform;
    private double currentHue = 0.0;


    public Game(SuperHexagonApp mainApp, Pane gamePane, Pane uiPane, Scene scene) {
        this.mainApp = mainApp;
        this.gamePane = gamePane;
        this.renderer = new Renderer(gamePane, uiPane);
        this.inputHandler = new InputHandler(scene);
        this.wallSpawner = new WallSpawner();

        this.worldRotationTransform = new Rotate(0, Config.CENTER_X, Config.CENTER_Y);
        this.gamePane.getTransforms().add(worldRotationTransform);
        System.out.println("Game instance created.");
    }


    private void initializeGame() {
        System.out.println("Initializing game state...");
        if (this.player != null && this.player.getShape() != null) renderer.removeGameObject(this.player.getShape());
        if (this.walls != null) { List<WallSegment> wallsToRemove = new ArrayList<>(this.walls); for(WallSegment wall : wallsToRemove) renderer.removeGameObject(wall.getShape()); }

        this.player = new Player();
        this.walls = new ArrayList<>();
        this.gameOver = false;
        this.isPaused = false;
        this.scoreTimeSeconds = 0;
        this.startTimeNanos = System.nanoTime();
        this.pauseStartTimeNanos = 0;
        this.currentWallSpeed = Config.INITIAL_WALL_SPEED_PIXELS_PER_FRAME;
        this.worldRotationAngle = 0;
        this.worldRotationTransform.setAngle(0);
        this.currentHue = randomStartingHue();
        this.wallSpawner.reset();
        this.inputHandler.reset();

        renderer.addGameObject(player.getShape());

        renderer.updateBackgroundColors(this.currentHue);
        renderer.render(scoreTimeSeconds, this.bestTimeSeconds, gameOver, isPaused);
        renderer.showPauseIndicator(false);
        System.out.println("Game Initialized/Reset complete. Best score for this session: " + String.format("%.2f", this.bestTimeSeconds));
    }

    private double randomStartingHue() { return Math.random() * 360; }

    public void start() {
        initializeGame();

        if (gameLoop != null) {
            gameLoop.stop();
            System.out.println("Stopped previous game loop.");
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (inputHandler.consumePauseToggle()) {
                    togglePause(now);
                    if (isPaused) { renderer.render(scoreTimeSeconds, bestTimeSeconds, gameOver, isPaused); return; }
                }

                if (gameOver) {
                    if (inputHandler.consumeRestartPress()) {
                        System.out.println("Restart requested via Space in handle loop.");
                        restart();
                        return;
                    } else if (inputHandler.consumeMenuRequest()) {
                        System.out.println("Return to menu requested via M in handle loop.");
                        returnToMenu();
                        return;
                    }
                    renderer.render(scoreTimeSeconds, bestTimeSeconds, gameOver, isPaused);
                    return;
                }

                if (isPaused) {
                    renderer.render(scoreTimeSeconds, bestTimeSeconds, gameOver, isPaused);
                    return;
                }


                worldRotationAngle = (worldRotationAngle + Config.WORLD_ROTATION_SPEED_DEG_PER_FRAME) % 360;
                worldRotationTransform.setAngle(worldRotationAngle);
                currentHue = (currentHue + Config.BACKGROUND_HUE_CHANGE_SPEED) % 360;
                renderer.updateBackgroundColors(currentHue);

                player.setRotatingLeft(inputHandler.isRotatingLeft());
                player.setRotatingRight(inputHandler.isRotatingRight());

                scoreTimeSeconds = (now - startTimeNanos) / 1_000_000_000.0;
                currentWallSpeed = Config.INITIAL_WALL_SPEED_PIXELS_PER_FRAME + scoreTimeSeconds * Config.SPEED_INCREASE_FACTOR_PER_SECOND;

                player.update(0);

                List<WallSegment> newWalls = wallSpawner.trySpawnWalls(now);
                for(WallSegment newWall : newWalls) { walls.add(newWall); renderer.addGameObject(newWall.getShape()); }
                Iterator<WallSegment> iterator = walls.iterator();
                while (iterator.hasNext()) {
                    WallSegment wall = iterator.next();
                    wall.update(currentWallSpeed);
                    if (wall.isMarkedForRemoval()) {
                        renderer.removeGameObject(wall.getShape());
                        iterator.remove();
                    }
                }

                checkCollisions();

                renderer.render(scoreTimeSeconds, bestTimeSeconds, gameOver, isPaused);
            }
        };
        gameLoop.start();
        System.out.println("Game loop started.");
    }

    private void togglePause(long now) {
        if (gameOver) return;

        isPaused = !isPaused;
        renderer.showPauseIndicator(isPaused);

        if (isPaused) {
            pauseStartTimeNanos = now;
            System.out.println("Game Paused.");
        } else {
            long pauseDurationNanos = now - pauseStartTimeNanos;
            startTimeNanos += pauseDurationNanos;
            System.out.println("Game Resumed.");
        }
    }

    private void checkCollisions() {
        if (gameOver || isPaused) return;
        double playerAbsoluteAngle = (player.getAngleDegrees() + worldRotationAngle + 360) % 360;
        for (WallSegment wall : walls) {
            double wallInnerEdge = wall.getCurrentDistance() - Config.WALL_THICKNESS / 2;
            double wallOuterEdge = wall.getCurrentDistance() + Config.WALL_THICKNESS / 2;
            if (player.getRadius() >= wallInnerEdge && player.getRadius() <= wallOuterEdge) {
                double wallAbsoluteStart = (wall.getStartAngleDeg() + worldRotationAngle + 360) % 360;
                double wallAbsoluteEnd = (wall.getEndAngleDeg() + worldRotationAngle + 360);
                if (isAngleBetween(playerAbsoluteAngle, wallAbsoluteStart, wallAbsoluteEnd)) {
                    if (!Shape.intersect(player.getShape(), wall.getShape()).getBoundsInLocal().isEmpty()) {
                        triggerGameOver();
                        return;
                    }
                }
            }
        }
    }

    private boolean isAngleBetween(double angle, double start, double end) {
        angle = (angle % 360 + 360) % 360;
        start = (start % 360 + 360) % 360;
        double normalizedEnd = (end % 360 + 360) % 360;
        if (start <= normalizedEnd) {
            return angle >= start && angle < normalizedEnd;
        } else {
            return angle >= start || angle < normalizedEnd;
        }
    }


    private void triggerGameOver() {
        if (!gameOver) {
            gameOver = true;

            if (scoreTimeSeconds > bestTimeSeconds) {
                bestTimeSeconds = scoreTimeSeconds;
                System.out.println("-> Session best score updated: " + String.format("%.2f", bestTimeSeconds));
            }

            System.out.println("Game Over! Final score: " + String.format("%.2f", scoreTimeSeconds));
            renderer.render(scoreTimeSeconds, bestTimeSeconds, gameOver, isPaused);

        }
    }


    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
            System.out.println("Game loop explicitly stopped.");
        }
    }


    public void restart() {
        System.out.println("Restart initiated...");
        stop();

        final double finalScore = this.scoreTimeSeconds;
        final String playerName = mainApp.getCurrentPlayerName();
        if (playerName != null && mainApp != null) {
            System.out.println("Requesting App to record result before restarting: P=" + playerName + ", S=" + finalScore);
            Platform.runLater(() -> mainApp.recordGameResult(playerName, finalScore));
        } else { System.err.println("Could not record result before restart. Missing PlayerName or MainApp reference."); }

        start();
    }


    private void returnToMenu() {
        System.out.println("Return to menu initiated...");
        stop();

        final double finalScore = this.scoreTimeSeconds;
        final String playerName = mainApp.getCurrentPlayerName();
        if (playerName != null && mainApp != null) {
            System.out.println("Requesting App to record result before returning to menu: P=" + playerName + ", S=" + finalScore);
            Platform.runLater(() -> {
                mainApp.recordGameResult(playerName, finalScore);
                mainApp.goToMainMenu();
            });
        } else {
            System.err.println("Could not record result. Returning to menu anyway...");
            Platform.runLater(() -> mainApp.goToMainMenu());
        }
    }



    public double getBestTimeSeconds() {
        return this.bestTimeSeconds;
    }


    public void setBestTimeSeconds(double initialBest) {
        this.bestTimeSeconds = initialBest;
        System.out.println("Initial Session Best set to: " + String.format("%.2f", this.bestTimeSeconds));
    }

}