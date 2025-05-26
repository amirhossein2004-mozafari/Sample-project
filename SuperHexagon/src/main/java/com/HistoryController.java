package com;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import java.util.Collections;
import java.util.List;


interface HistoryListener {
    void onHistoryScreenClosed();
}


public class HistoryController {

    @FXML
    private ListView<String> historyListView;

    @FXML
    private Button backButton;

    private HistoryListener historyListener;


    public void setHistoryListener(HistoryListener listener) {
        this.historyListener = listener;
    }


    public void populateHistory(List<GameRecord> records) {
        if (historyListView == null) {
            System.err.println("History ListView is null in populateHistory!");
            return;
        }
        if (records == null || records.isEmpty()) {
            historyListView.setPlaceholder(new javafx.scene.control.Label("No game history yet."));
            historyListView.setItems(FXCollections.observableArrayList());
            System.out.println("Populating history: No records.");
        } else {
            Collections.reverse(records);

            ObservableList<String> items = FXCollections.observableArrayList();
            for (GameRecord record : records) {
                items.add(record.toString());
            }
            historyListView.setItems(items);
            System.out.println("Populating history with " + records.size() + " records.");
        }
    }


    @FXML
    void handleBack(ActionEvent event) {
        System.out.println("Back button clicked in History screen.");
        if (historyListener != null) {
            historyListener.onHistoryScreenClosed();
        } else {
            System.err.println("Error: HistoryListener is not set in HistoryController!");
        }
    }


    @FXML
    void initialize() {
        System.out.println("HistoryController initialized.");
    }
}