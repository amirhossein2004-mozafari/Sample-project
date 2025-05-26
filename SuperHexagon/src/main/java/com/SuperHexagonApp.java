package com;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class SuperHexagonApp extends Application implements StartGameListener, MainMenuListener, SettingsListener, HistoryListener {

    private Stage primaryStage;
    private Game game;
    private String currentPlayerName;
    private double overallBestScore = 0.0;
    private MediaPlayer backgroundMusicPlayer;

    private boolean isMusicEnabled = true;
    private boolean isHistoryEnabled = true;

    private List<GameRecord> gameHistoryList = new ArrayList<>();
    private static final String HISTORY_FILE_NAME = "game_history.json";
    private Gson gson;


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Super Hexagon");

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        loadGameHistory();

        initBackgroundMusic();

        loadMainMenuScreen();
    }


    @Override
    public void stop() throws Exception {
        System.out.println("Application stop requested...");
        if (game != null) {
            updateOverallBestScore(game.getBestTimeSeconds());
            game.stop();
            game = null;
        }
        stopMusic();
        saveGameHistory();
        super.stop();
        System.out.println("Application stopped cleanly.");
    }


    private void initBackgroundMusic() {
        try {
            URL musicResource = getClass().getResource("/background_music.mp3");
            if (musicResource == null) { handleFxmlLoadError("Background Music File", "/background_music.mp3"); return; }

            Media backgroundMedia = new Media(musicResource.toString());
            backgroundMusicPlayer = new MediaPlayer(backgroundMedia);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.3);
            backgroundMusicPlayer.setOnError(() -> {
                System.err.println("MediaPlayer Error: " + backgroundMusicPlayer.getError());
                showErrorAlert("Music Playback Error", "Could not play background music.");
            });

            if (this.isMusicEnabled) {
                playMusic();
            }
            System.out.println("Background music initialized.");

        } catch (MediaException e) { System.err.println("Media Init Error: " + e.getMessage()); e.printStackTrace(); showErrorAlert("Music Error", "Failed to initialize media player."); }
        catch (Exception e) { System.err.println("Unexpected Music Init Error: " + e.getMessage()); e.printStackTrace(); showErrorAlert("Music Error", "Unexpected error during music setup."); }
    }


    private void playMusic() {
        if (backgroundMusicPlayer != null && isMusicEnabled && backgroundMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            backgroundMusicPlayer.play();
            System.out.println("Music playback started/resumed.");
        }
    }


    private void stopMusic() {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            backgroundMusicPlayer.stop(); // متوقف کردن کامل (به جای pause)
            System.out.println("Music playback stopped.");
        }
    }



    private void loadGameHistory() {
        String historyFilePath = HISTORY_FILE_NAME;
        if (!Files.exists(Paths.get(historyFilePath))) {
            System.out.println("History file (" + historyFilePath + ") not found. Starting with empty history.");
            this.gameHistoryList = new ArrayList<>();
            this.overallBestScore = 0.0;
            return;
        }
        try (Reader reader = new FileReader(historyFilePath)) {
            Type listType = new TypeToken<ArrayList<GameRecord>>() {}.getType();
            this.gameHistoryList = gson.fromJson(reader, listType);
            if (this.gameHistoryList == null) {
                this.gameHistoryList = new ArrayList<>();
                System.out.println("History file was empty or invalid. Initialized new list.");
            } else {
                System.out.println("Loaded " + gameHistoryList.size() + " records from history file.");
                updateBestScoreFromHistory();
            }
        } catch (IOException e) {
            System.err.println("IOException reading history file: " + e.getMessage());
            showErrorAlert("History Load Error", "Could not read game history.");
            this.gameHistoryList = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error parsing history file: " + e.getMessage());
            showErrorAlert("History Parse Error", "Game history file is corrupted.");
            this.gameHistoryList = new ArrayList<>();
        }
        if (this.gameHistoryList == null) this.gameHistoryList = new ArrayList<>();
    }


    private void saveGameHistory() {
        if (!this.isHistoryEnabled) {
            System.out.println("History saving is disabled by settings. Skipping save.");
            return;
        }
        if (this.gameHistoryList == null) { System.err.println("Error: Cannot save null history list!"); return; }

        try (Writer writer = new FileWriter(HISTORY_FILE_NAME)) {
            gson.toJson(this.gameHistoryList, writer);
            System.out.println("Saved " + gameHistoryList.size() + " records to " + HISTORY_FILE_NAME);
        } catch (IOException e) {
            System.err.println("Error saving game history to file: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("History Save Error", "Could not save game history.");
        }
    }


    private void updateBestScoreFromHistory() {
        this.overallBestScore = this.gameHistoryList.stream()
                .filter(record -> record != null)
                .mapToDouble(GameRecord::getScore)
                .max()
                .orElse(0.0);
        System.out.println("Overall best score updated from history: " + String.format("%.2f", this.overallBestScore));
    }



    private void loadMainMenuScreen() {
        System.out.println("Loading Main Menu Screen...");
        try {
            URL fxmlLoc = getClass().getResource("/MainMenu.fxml");
            if (fxmlLoc == null) { handleFxmlLoadError("MainMenu.fxml", "/MainMenu.fxml"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlLoc);
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            if (controller != null) {
                controller.setMainMenuListener(this);
                controller.setBestScore(this.overallBestScore);
            } else { handleControllerError("MainMenuController"); return; }
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Super Hexagon - Main Menu");
            primaryStage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0); });
            if (!primaryStage.isShowing()) primaryStage.show();
        } catch (Exception e) { handleGenericLoadError("Main Menu", e); }
    }

    private void loadPreGameScreen() {
        System.out.println("Loading Pre-Game Screen...");
        try {
            URL fxmlLoc = getClass().getResource("/PreGame.fxml");
            if (fxmlLoc == null) { handleFxmlLoadError("PreGame.fxml", "/PreGame.fxml"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlLoc);
            Parent root = loader.load();
            PreGameController controller = loader.getController();
            if (controller != null) { controller.setStartGameListener(this); }
            else { handleControllerError("PreGameController"); return; }
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Super Hexagon - Enter Name");
            primaryStage.setOnCloseRequest(e -> { loadMainMenuScreen(); e.consume(); });
        } catch (Exception e) { handleGenericLoadError("Pre-Game", e); }
    }

    private void loadSettingsScreen() {
        System.out.println("Loading Settings Screen...");
        try {
            URL fxmlLoc = getClass().getResource("/Settings.fxml");
            if (fxmlLoc == null) { handleFxmlLoadError("Settings.fxml", "/Settings.fxml"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlLoc);
            Parent root = loader.load();
            SettingsController controller = loader.getController();
            if (controller != null) {
                controller.setSettingsListener(this);
                controller.initializeSettings(isMusicEnabled, isHistoryEnabled);
            } else { handleControllerError("SettingsController"); return; }
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Super Hexagon - Settings");
            primaryStage.setOnCloseRequest(e -> { loadMainMenuScreen(); e.consume(); });
        } catch (Exception e) { handleGenericLoadError("Settings", e); }
    }

    private void loadHistoryScreen() {
        System.out.println("Loading History Screen...");
        try {
            URL fxmlLoc = getClass().getResource("/History.fxml");
            if (fxmlLoc == null) { handleFxmlLoadError("History.fxml", "/History.fxml"); return; }
            FXMLLoader loader = new FXMLLoader(fxmlLoc);
            Parent root = loader.load();
            HistoryController controller = loader.getController();
            if (controller != null) {
                controller.setHistoryListener(this);
                controller.populateHistory(this.gameHistoryList);
            } else { handleControllerError("HistoryController"); return; }
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Super Hexagon - History");
            primaryStage.setOnCloseRequest(e -> { loadMainMenuScreen(); e.consume(); });
        } catch (Exception e) { handleGenericLoadError("History", e); }
    }



    @Override
    public void startGameRequested(String playerName) {
        this.currentPlayerName = playerName;
        System.out.println("Starting game requested for: " + playerName);
        try {
            StackPane root=new StackPane(); Pane gamePane=new Pane(); Pane uiPane=new Pane();
            uiPane.setPickOnBounds(false); root.getChildren().addAll(gamePane, uiPane);
            Scene gameScene=new Scene(root, Config.WIDTH, Config.HEIGHT);

            game = new Game(this, gamePane, uiPane, gameScene);
            game.setBestTimeSeconds(this.overallBestScore);
            game.start();

            primaryStage.setTitle("Playing: " + playerName);
            primaryStage.setScene(gameScene);

            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Game window close requested by user (mid-game).");
                if (game != null) {
                    updateOverallBestScore(game.getBestTimeSeconds());
                    game.stop();
                    game = null;
                }
                loadMainMenuScreen();
                event.consume();
            });
        } catch (Exception e) {
            System.err.println("Fatal error occurred while starting the game!");
            e.printStackTrace();
            showErrorAlert("Game Start Failed", "Could not initialize or start the game.\n" + e.getMessage());
            loadMainMenuScreen();
        }
    }

    @Override public void onNewGameRequested() { loadPreGameScreen(); }
    @Override public void onHistoryRequested() { loadHistoryScreen(); }
    @Override public void onSettingsRequested() { loadSettingsScreen(); }
    @Override public void onExitRequested() { Platform.exit(); System.exit(0); }

    @Override public void onMusicSettingChanged(boolean enabled) { this.isMusicEnabled = enabled; if(enabled) playMusic(); else stopMusic();  }
    @Override public void onHistorySettingChanged(boolean enabled) { this.isHistoryEnabled = enabled; }
    @Override public void onSettingsClosed() { loadMainMenuScreen(); }

    @Override public void onHistoryScreenClosed() { loadMainMenuScreen(); }


    public void recordGameResult(String playerName, double finalScore) {
        System.out.println("App received completed game result for '" + playerName + "': " + finalScore);

        if (this.isHistoryEnabled) {
            GameRecord newRecord = new GameRecord(playerName, finalScore, LocalDateTime.now());
            if (this.gameHistoryList == null) this.gameHistoryList = new ArrayList<>();
            this.gameHistoryList.add(newRecord);
            System.out.println("Record added to history list.");
            saveGameHistory();
        } else {
            System.out.println("History saving disabled, record not added to list/file.");
        }

        updateOverallBestScore(finalScore);

        this.game = null;

    }



    public void goToMainMenu() {
        System.out.println("Request to return to Main Menu received.");
        if (this.game != null) {
            System.out.println("Stopping potentially active game before returning to menu...");
            this.game.stop();
            this.game = null;
        }
        loadMainMenuScreen();
    }


    public String getCurrentPlayerName() { return currentPlayerName; }
    private void updateOverallBestScore(double currentBestScore) {  }
    private void showErrorAlert(String t, String m){
        Platform.runLater(()->{Alert a=new Alert(Alert.AlertType.ERROR);
            a.setTitle(t);
            a.setHeaderText(null);
            a.setContentText(m);
            a.showAndWait();});
    }
    private void showInfoAlert(String t, String m){
        Platform.runLater(()->{
            Alert a=new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(t);
            a.setHeaderText(null);
            a.setContentText(m);
            a.showAndWait();
        });
    }
    private void handleFxmlLoadError(String f, String p){ System.err.println("FNF Error: "+f+" at "+p); showErrorAlert("FNF", "Cannot find: "+f); }
    private void handleControllerError(String c){ System.err.println("Controller Error: "+c); showErrorAlert("Init Error", "Cannot init controller: "+c); }
    private void handleGenericLoadError(String s, Exception e){ System.err.println("Load Error: "+s); e.printStackTrace(); showErrorAlert("Load Error", "Cannot load "+s+":\n"+e.getMessage());}

    public static void main(String[] args) {
        launch(args);
    }
}