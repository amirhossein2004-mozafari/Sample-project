package com;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class Renderer {

    private final Pane gamePane;
    private final Pane uiPane;

    private Polygon centerHexagon;
    private Polygon[] backgroundSegments;

    private Text scoreLabel;
    private Text scoreValue;
    private Text bestScoreLabel;
    private Text bestScoreValue;
    private Text pauseText;
    private Text gameOverText;


    public Renderer(Pane gamePane, Pane uiPane) {
        this.gamePane = gamePane;
        this.uiPane = uiPane;

        setupBackground(this.gamePane);
        setupStaticGameElements(this.gamePane);
        setupUIElements(this.uiPane);
    }


    private void setupBackground(Pane targetPane) {
        backgroundSegments = new Polygon[Config.SIDES];
        double outerRadius = Math.sqrt(Math.pow(Config.WIDTH / 2.0, 2) + Math.pow(Config.HEIGHT / 2.0, 2)) * 1.1;

        for (int i = 0; i < Config.SIDES; i++) {
            double startAngleRad = Math.toRadians(i * 60.0 - 90.0);
            double endAngleRad = Math.toRadians((i + 1) * 60.0 - 90.0);
            double centerX = Config.CENTER_X; double centerY = Config.CENTER_Y;

            double outerStartX = centerX + outerRadius * Math.cos(startAngleRad);
            double outerStartY = centerY + outerRadius * Math.sin(startAngleRad);
            double outerEndX = centerX + outerRadius * Math.cos(endAngleRad);
            double outerEndY = centerY + outerRadius * Math.sin(endAngleRad);

            backgroundSegments[i] = new Polygon(centerX, centerY, outerStartX, outerStartY, outerEndX, outerEndY);

            targetPane.getChildren().add(0, backgroundSegments[i]);
        }
    }



    private void setupStaticGameElements(Pane targetPane) {
        centerHexagon = createHexagon(Config.CENTER_X, Config.CENTER_Y, Config.CENTER_HEX_RADIUS, Config.CENTER_HEX_COLOR);
        if (targetPane.getChildren().size() >= Config.SIDES) {
            targetPane.getChildren().add(Config.SIDES, centerHexagon);
        } else {
            targetPane.getChildren().add(centerHexagon);
        }
    }


    private void setupUIElements(Pane targetPane) {
        Font uiFont = Font.font("Arial", Config.SCORE_TEXT_FONT_SIZE);
        Font largeFont = Font.font("Arial", Config.GAME_OVER_FONT_SIZE);

        scoreLabel = new Text(10, 30, "TIME:");
        scoreLabel.setFont(uiFont);
        scoreLabel.setFill(Config.SCORE_TEXT_COLOR);
        scoreValue = new Text(80, 30, "0.00");
        scoreValue.setFont(uiFont);
        scoreValue.setFill(Config.SCORE_TEXT_COLOR);

        bestScoreLabel = new Text(10, 55, "BEST:");
        bestScoreLabel.setFont(uiFont);
        bestScoreLabel.setFill(Config.SCORE_TEXT_COLOR);
        bestScoreValue = new Text(80, 55, "0.00");
        bestScoreValue.setFont(uiFont);
        bestScoreValue.setFill(Config.SCORE_TEXT_COLOR);

        pauseText = new Text("PAUSED");
        pauseText.setFont(largeFont);
        pauseText.setFill(Color.YELLOW);
        pauseText.setTextAlignment(TextAlignment.CENTER);
        pauseText.setVisible(false);
        pauseText.layoutXProperty().bind(targetPane.widthProperty().subtract(pauseText.getLayoutBounds().getWidth()).divide(2));
        pauseText.layoutYProperty().bind(targetPane.heightProperty().subtract(pauseText.getLayoutBounds().getHeight()).divide(2));

        gameOverText = new Text("GAME OVER\nSpace: Restart / M: Main Menu");
        gameOverText.setFont(largeFont);
        gameOverText.setFill(Config.GAME_OVER_TEXT_COLOR);
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        gameOverText.setVisible(false);

        gameOverText.layoutXProperty().bind(targetPane.widthProperty().subtract(gameOverText.getLayoutBounds().getWidth()).divide(2));
        gameOverText.layoutYProperty().bind(targetPane.heightProperty().subtract(gameOverText.getLayoutBounds().getHeight()).divide(2));

        targetPane.getChildren().addAll(scoreLabel, scoreValue, bestScoreLabel, bestScoreValue, pauseText, gameOverText);
    }



    public void updateBackgroundColors(double currentHue) {
        Color baseColor = Color.hsb(currentHue, Config.BACKGROUND_SATURATION, Config.BACKGROUND_BRIGHTNESS);
        Color lightColor = baseColor.deriveColor(0, 1, Config.BACKGROUND_ALT_BRIGHTNESS_FACTOR, 1);
        Color darkColor = baseColor.deriveColor(0, 1, 1.0 / Config.BACKGROUND_ALT_BRIGHTNESS_FACTOR, 1);

        for (int i = 0; i < Config.SIDES; i++) {

            if (backgroundSegments != null && i < backgroundSegments.length && backgroundSegments[i] != null) {
                backgroundSegments[i].setFill((i % 2 == 0) ? darkColor : lightColor);
            }
        }
    }


    public void showPauseIndicator(boolean show) {
        if (pauseText != null) {
            pauseText.setVisible(show);
        }
    }



    public void render(double currentScore, double bestScore, boolean isGameOver, boolean isPaused) {
        if (scoreValue != null) {
            scoreValue.setText(String.format("%.2f", currentScore));
        }
        if (bestScoreValue != null) {
            bestScoreValue.setText(String.format("%.2f", bestScore));
        }
        if (gameOverText != null) {
            gameOverText.setVisible(isGameOver);
        }
    }



    public void addGameObject(Polygon shape) {
        if (shape != null && gamePane != null && !gamePane.getChildren().contains(shape)) {
            gamePane.getChildren().add(shape);
        }
    }


    public void removeGameObject(Polygon shape) {
        if (shape != null && gamePane != null) {
            gamePane.getChildren().remove(shape);
        }
    }



    private Polygon createHexagon(double centerX, double centerY, double radius, Color color) {
        Polygon hexagon = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            hexagon.getPoints().addAll(
                    centerX + radius * Math.cos(angle),
                    centerY + radius * Math.sin(angle)
            );
        }
        hexagon.setFill(null);
        hexagon.setStroke(color);
        hexagon.setStrokeWidth(Config.CENTER_HEX_STROKE_WIDTH);
        return hexagon;
    }

}