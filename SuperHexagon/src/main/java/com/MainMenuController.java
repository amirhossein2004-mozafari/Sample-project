package com;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


interface MainMenuListener {
    void onNewGameRequested();
    void onHistoryRequested();
    void onSettingsRequested();
    void onExitRequested();
}


public class MainMenuController {

    @FXML
    private Label bestScoreValueLabel;

    @FXML
    private Button newGameButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button exitButton;


    private MainMenuListener menuListener;


    public void setMainMenuListener(MainMenuListener listener) {
        this.menuListener = listener;
    }


    public void setBestScore(double score) {
        if (bestScoreValueLabel != null) {
            bestScoreValueLabel.setText(String.format("%.2f", score));
        }
    }


    @FXML
    void handleNewGame(ActionEvent event) {
        System.out.println("New Game button clicked.");
        if (menuListener != null) {
            menuListener.onNewGameRequested();
        } else { logListenerError(); }
    }

    @FXML
    void handleHistory(ActionEvent event) {
        System.out.println("History button clicked.");
        if (menuListener != null) {
            menuListener.onHistoryRequested();
        } else { logListenerError(); }
    }

    @FXML
    void handleSettings(ActionEvent event) {
        System.out.println("Settings button clicked.");
        if (menuListener != null) {
            menuListener.onSettingsRequested();
        } else { logListenerError(); }
    }

    @FXML
    void handleExit(ActionEvent event) {
        System.out.println("Exit button clicked.");
        if (menuListener != null) {
            menuListener.onExitRequested();
        } else { logListenerError(); }
    }

    private void logListenerError() {
        System.err.println("Error: MainMenuListener is not set in MainMenuController!");
    }


    @FXML
    void initialize() {
        System.out.println("MainMenuController initialized.");
    }
}