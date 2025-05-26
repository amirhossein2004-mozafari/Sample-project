package com;

import javafx.scene.shape.Polygon;

public class Player {
    private double angleDegrees;
    private final Polygon shape;
    private boolean rotatingLeft = false;
    private boolean rotatingRight = false;

    public Player() {
        this.angleDegrees = 0;
        this.shape = createPlayerTriangle();
        updatePosition();
    }

    private Polygon createPlayerTriangle() {
        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(
                0.0, -Config.PLAYER_SIZE, // Top point
                -Config.PLAYER_SIZE / 1.5, Config.PLAYER_SIZE / 2.0,
                Config.PLAYER_SIZE / 1.5, Config.PLAYER_SIZE / 2.0
        );
        triangle.setFill(Config.PLAYER_COLOR);
        return triangle;
    }

    public void update(double deltaTime) {
        if (rotatingLeft) {
            angleDegrees -= Config.PLAYER_ROTATION_SPEED_DEG_PER_FRAME;
        }
        if (rotatingRight) {
            angleDegrees += Config.PLAYER_ROTATION_SPEED_DEG_PER_FRAME;
        }

        angleDegrees = (angleDegrees % 360 + 360) % 360;
        updatePosition();
    }

    private void updatePosition() {
        double angleRad = Math.toRadians(angleDegrees - 90);
        double playerCenterX = Config.CENTER_X + Config.PLAYER_RADIUS_FROM_CENTER * Math.cos(angleRad);
        double playerCenterY = Config.CENTER_Y + Config.PLAYER_RADIUS_FROM_CENTER * Math.sin(angleRad);

        shape.setTranslateX(playerCenterX);
        shape.setTranslateY(playerCenterY);
        shape.setRotate(angleDegrees);
    }

    public Polygon getShape() {
        return shape;
    }

    public double getAngleDegrees() {
        return angleDegrees;
    }

    public double getRadius() {
        return Config.PLAYER_RADIUS_FROM_CENTER;
    }

    public void setRotatingLeft(boolean rotatingLeft) {
        this.rotatingLeft = rotatingLeft;
    }

    public void setRotatingRight(boolean rotatingRight) {
        this.rotatingRight = rotatingRight;
    }

    public void reset() {
        this.angleDegrees = 0;
        this.rotatingLeft = false;
        this.rotatingRight = false;
        updatePosition();
    }
}