package com;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;


interface SettingsListener {
    void onMusicSettingChanged(boolean enabled);
    void onHistorySettingChanged(boolean enabled);
    void onSettingsClosed();
}


public class SettingsController {

    @FXML
    private CheckBox musicCheckBox;

    @FXML
    private CheckBox historyCheckBox;

    @FXML
    private Button backButton;

    private SettingsListener settingsListener;


    public void setSettingsListener(SettingsListener listener) {
        this.settingsListener = listener;
    }


    public void initializeSettings(boolean musicEnabled, boolean historyEnabled) {
        if (musicCheckBox != null) {
            musicCheckBox.setSelected(musicEnabled);
        }
        if (historyCheckBox != null) {
            historyCheckBox.setSelected(historyEnabled);
        }
        System.out.println("Settings screen initialized. Music: " + musicEnabled + ", History: " + historyEnabled);
    }


    @FXML
    void handleMusicToggle(ActionEvent event) {
        if (settingsListener != null && musicCheckBox != null) {
            boolean isSelected = musicCheckBox.isSelected();
            System.out.println("Music CheckBox toggled to: " + isSelected);
            settingsListener.onMusicSettingChanged(isSelected);
        } else { logListenerError(); }
    }


    @FXML
    void handleHistoryToggle(ActionEvent event) {
        if (settingsListener != null && historyCheckBox != null) {
            boolean isSelected = historyCheckBox.isSelected();
            System.out.println("History CheckBox toggled to: " + isSelected);
            settingsListener.onHistorySettingChanged(isSelected);
        } else { logListenerError(); }
    }


    @FXML
    void handleBack(ActionEvent event) {
        System.out.println("Back button clicked.");
        if (settingsListener != null) {
            settingsListener.onSettingsClosed();
        } else { logListenerError(); }
    }


    private void logListenerError() {
        System.err.println("Error: SettingsListener is not set in SettingsController!");
    }


    @FXML
    void initialize() {
        System.out.println("SettingsController initialized.");
    }
}