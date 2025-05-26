package com;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;

public class WallSegment {
    private double startAngleDeg;
    private double endAngleDeg;
    private double currentDistance;
    private final Polygon shape;
    private boolean markedForRemoval = false;

    public WallSegment(double startAngleDeg, double endAngleDeg, double initialDistance) {
        this.startAngleDeg = (startAngleDeg % 360 + 360) % 360;
        this.endAngleDeg = endAngleDeg;


        if (this.endAngleDeg <= this.startAngleDeg && (this.endAngleDeg % 360 != this.startAngleDeg % 360)) {
            this.endAngleDeg += 360;
        }


        this.currentDistance = initialDistance;
        this.shape = new Polygon();
        this.shape.setFill(Config.WALL_COLOR);
        updateShapePoints();
    }

    public void update(double speed) {
        currentDistance -= speed;
        if (currentDistance < Config.CENTER_HEX_RADIUS - Config.WALL_THICKNESS) {
            markedForRemoval = true;
        } else {
            updateShapePoints();
        }
    }

    private void updateShapePoints() {
        shape.getPoints().clear();

        double innerRadius = Math.max(0, currentDistance - Config.WALL_THICKNESS / 2);
        double outerRadius = currentDistance + Config.WALL_THICKNESS / 2;



        double startRadNorm = Math.toRadians(startAngleDeg - 90);

        double endRadNorm = Math.toRadians((endAngleDeg % 360 + ((endAngleDeg >= 360)? 360 : 0) ) - 90 );

        if(endAngleDeg > 0 && endAngleDeg % 360 == 0) endRadNorm = Math.toRadians(360-90);


        Point2D p1 = pointOnCircle(Config.CENTER_X, Config.CENTER_Y, innerRadius, startRadNorm);
        Point2D p2 = pointOnCircle(Config.CENTER_X, Config.CENTER_Y, outerRadius, startRadNorm);
        Point2D p3 = pointOnCircle(Config.CENTER_X, Config.CENTER_Y, outerRadius, endRadNorm);
        Point2D p4 = pointOnCircle(Config.CENTER_X, Config.CENTER_Y, innerRadius, endRadNorm);

        shape.getPoints().addAll(p1.getX(), p1.getY(),
                p2.getX(), p2.getY(),
                p3.getX(), p3.getY(),
                p4.getX(), p4.getY());
    }


    private Point2D pointOnCircle(double cx, double cy, double radius, double angleRad) {
        return new Point2D(cx + radius * Math.cos(angleRad),
                cy + radius * Math.sin(angleRad));
    }



    public Polygon getShape() {
        return shape;
    }

    public double getCurrentDistance() {
        return currentDistance;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public double getStartAngleDeg() {
        return startAngleDeg;
    }

    public double getEndAngleDeg() {
        return endAngleDeg;
    }

}