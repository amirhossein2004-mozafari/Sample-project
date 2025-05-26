package com;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


interface StartGameListener {

    void startGameRequested(String playerName);
}


public class PreGameController {

    @FXML
    private TextField playerNameField;

    @FXML
    private Button startButton;

    private StartGameListener startGameListener;


    public void setStartGameListener(StartGameListener listener) {
        this.startGameListener = listener;
    }


    @FXML
    void handleStartGame(ActionEvent event) {
        String playerName = playerNameField.getText().trim();

        if (playerName.isEmpty()) {
            showAlert("Name Required", "Please enter your player name to start.");
            playerNameField.requestFocus();
        } else {
            if (startGameListener != null) {
                System.out.println("Start button clicked. Player name: " + playerName);
                startGameListener.startGameRequested(playerName);
            } else {
                System.err.println("ERROR: StartGameListener is null in PreGameController!");
                showAlert("Internal Error", "Cannot start game because the listener is not configured.");
            }
        }
    }

    @FXML
    void initialize() {
        System.out.println("PreGameController has been initialized.");
        playerNameField.requestFocus();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}