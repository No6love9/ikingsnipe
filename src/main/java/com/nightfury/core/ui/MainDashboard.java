package com.nightfury.core.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import com.nightfury.core.logic.TradeManager;

/**
 * The Professional GUI: MainDashboard.java
 * A basic JavaFX dashboard structure with the "Chrome" aesthetic.
 */
public class MainDashboard extends Application {

    private static TradeManager tradeManager;
    private TextArea consoleArea;
    private Label statusLabel;

    public static void launchUI(String[] args, TradeManager manager) {
        tradeManager = manager;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("iKingSnipe - Manus System C2");

        // --- Root Layout ---
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // --- Sidebar (Variable Tweak) ---
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // --- Main Content (Console) ---
        VBox mainContent = createMainContent();
        root.setCenter(mainContent);

        // --- Scene Setup ---
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/ChromeTheme.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Start a simple UI update thread (for demonstration)
        startStatusUpdater();
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #2d2d2d; -fx-border-width: 0 1 0 0;");

        Label title = new Label("Variable Tweak");
        title.getStyleClass().add("title-label");
        
        // Status
        statusLabel = new Label("Status: IDLE");
        statusLabel.setStyle("-fx-text-fill: #00ffc3;");

        // Base Delay Control
        Label delayLabel = new Label("Base Delay (ms):");
        TextField delayField = new TextField("5000");
        Button setDelayButton = new Button("Set Delay");
        setDelayButton.setOnAction(e -> {
            try {
                double delay = Double.parseDouble(delayField.getText());
                if (tradeManager != null) {
                    tradeManager.setBaseDelay(delay);
                    consoleArea.appendText("[UI] Base Delay set to " + delay + "ms\n");
                }
            } catch (NumberFormatException ex) {
                consoleArea.appendText("[UI Error] Invalid number for delay.\n");
            }
        });
        
        // Auto Accept Toggle
        CheckBox autoAcceptCheck = new CheckBox("Auto Accept Trades");
        autoAcceptCheck.setSelected(true);
        autoAcceptCheck.setOnAction(e -> {
            if (tradeManager != null) {
                tradeManager.setAutoAccept(autoAcceptCheck.isSelected());
                consoleArea.appendText("[UI] Auto Accept set to " + autoAcceptCheck.isSelected() + "\n");
            }
        });
        
        // Test Action Button
        Button testActionButton = new Button("Execute Test Trade");
        testActionButton.setOnAction(e -> {
            if (tradeManager != null) {
                tradeManager.executeTradeAction("TestPlayer_001");
                consoleArea.appendText("[UI] Test trade action queued.\n");
            }
        });

        sidebar.getChildren().addAll(title, new Separator(), statusLabel, delayLabel, delayField, setDelayButton, autoAcceptCheck, new Separator(), testActionButton);
        return sidebar;
    }

    private VBox createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label consoleTitle = new Label("Real-Time Console");
        consoleTitle.getStyleClass().add("title-label");

        consoleArea = new TextArea();
        consoleArea.setEditable(false);
        consoleArea.setId("console-area");
        consoleArea.setWrapText(true);
        VBox.setVgrow(consoleArea, Priority.ALWAYS);
        
        content.getChildren().addAll(consoleTitle, consoleArea);
        return content;
    }
    
    private void startStatusUpdater() {
        Thread updater = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(500); // Update twice a second
                    if (tradeManager != null) {
                        javafx.application.Platform.runLater(() -> {
                            statusLabel.setText("Status: " + tradeManager.getCurrentState());
                        });
                    }
                }
            } catch (InterruptedException e) {
                // Thread interrupted, shutting down
            }
        });
        updater.setDaemon(true);
        updater.start();
    }
}
