package com;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class InputHandler {
    private boolean rotatingLeft = false;
    private boolean rotatingRight = false;
    private boolean restartPressed = false;
    private boolean pauseToggleRequested = false;
    private boolean menuRequested = false;

    public InputHandler(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) rotatingLeft = true;
            else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) rotatingRight = true;
            else if (event.getCode() == KeyCode.SPACE) restartPressed = true;
            else if (event.getCode() == KeyCode.ESCAPE) pauseToggleRequested = true;
            else if (event.getCode() == KeyCode.M) menuRequested = true;
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) rotatingLeft = false;
            else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) rotatingRight = false;
        });
    }

    public boolean isRotatingLeft() { return rotatingLeft; }
    public boolean isRotatingRight() { return rotatingRight; }

    public boolean consumeRestartPress() { boolean p = restartPressed; restartPressed = false; return p; }
    public boolean consumePauseToggle() { boolean p = pauseToggleRequested; pauseToggleRequested = false; return p; }
    public boolean consumeMenuRequest() { boolean p = menuRequested; menuRequested = false; return p; }

    public void reset() {
        rotatingLeft = false; rotatingRight = false; restartPressed = false; pauseToggleRequested = false; menuRequested = false;
    }
}