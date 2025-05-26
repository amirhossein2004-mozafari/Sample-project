package com;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WallSpawner {

    private long lastWallSpawnTime = 0;
    private final Random random = new Random();

    public List<WallSegment> trySpawnWalls(long currentTimeNanos) {
        List<WallSegment> newWalls = new ArrayList<>();
        if (currentTimeNanos - lastWallSpawnTime > Config.WALL_SPAWN_INTERVAL_NANOS) {
            spawnWallPattern(newWalls);
            lastWallSpawnTime = currentTimeNanos;
        }
        return newWalls;
    }

    private void spawnWallPattern(List<WallSegment> wallList) {
        double anglePerSide = 360.0 / Config.SIDES;
        if (random.nextDouble() < Config.WALL_PATTERN_ALTERNATING_PROBABILITY) {
            System.out.println("Spawning Alternating Pattern");
            for (int i = 0; i < Config.SIDES; i++) {
                if (i % 2 == 0) {
                    double startAngle = i * anglePerSide;
                    double endAngle = (i + 1) * anglePerSide;
                    WallSegment segment = new WallSegment(startAngle, endAngle, Config.WALL_START_DISTANCE);
                    wallList.add(segment);
                }

            }
        } else {
            System.out.println("Spawning Single Gap Pattern");
            int gapIndex = random.nextInt(Config.SIDES);
            for (int i = 0; i < Config.SIDES; i++) {
                if (i == gapIndex) {
                    continue;
                }
                double startAngle = i * anglePerSide;
                double endAngle = (i + 1) * anglePerSide;
                WallSegment segment = new WallSegment(startAngle, endAngle, Config.WALL_START_DISTANCE);
                wallList.add(segment);
            }
        }
    }

    public void reset() {
        lastWallSpawnTime = System.nanoTime();
    }
}