package ui.gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import ui.api.Colors;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import game.api.*;
import java.util.List;
import java.util.ArrayList;

public class GameWindow {
    private Scene scene;
    private MapRenderer mapRenderer;
    private TextArea messageArea;
    private VBox playerInfoPanel;
    private Label turnLabel;
    private Label currentPlayerLabel;

    // 嵌入式用户输入面板
    private VBox userInputPanel;
    private Label inputPromptLabel;

    // 通知列表系统
    private VBox notificationContainer;
    private List<NotificationItem> activeNotifications;

    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final int NOTIFICATION_SPACING = 10;
    private static final int RIGHT_PANEL_WIDTH = 300;
    private static final int BOTTOM_PANEL_HEIGHT = 120;

    public GameWindow() {
        activeNotifications = new ArrayList<>();

        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(10));
        mainPane.setStyle("-fx-background-color: #f0f0f0;");

        // 顶部信息栏
        VBox topPanel = createTopPanel();
        mainPane.setTop(topPanel);

        // 中央地图区域
        mapRenderer = new MapRenderer();
        StackPane mapContainer = new StackPane(mapRenderer.getCanvas());
        mapContainer.setStyle("-fx-background-color: white;");
        mainPane.setCenter(mapContainer);

        // 右侧面板
        VBox rightPanelContent = new VBox(10);
        rightPanelContent.setPadding(new Insets(10));

        userInputPanel = createUserInputPanel();
        userInputPanel.setVisible(false);
        userInputPanel.setManaged(false);

        playerInfoPanel = createPlayerInfoPanel();
        rightPanelContent.getChildren().addAll(userInputPanel, playerInfoPanel);

        // 将右侧面板包装在 ScrollPane 中
        ScrollPane rightScrollPane = new ScrollPane(rightPanelContent);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScrollPane.setPrefWidth(RIGHT_PANEL_WIDTH);
        rightScrollPane.setMinWidth(RIGHT_PANEL_WIDTH);
        rightScrollPane.setMaxWidth(RIGHT_PANEL_WIDTH);
        rightScrollPane.setStyle("-fx-background-color: transparent;");

        mainPane.setRight(rightScrollPane);

        // 底部消息面板（固定高度）
        VBox bottomPanel = createBottomPanel();
        bottomPanel.setPrefHeight(BOTTOM_PANEL_HEIGHT);
        bottomPanel.setMinHeight(BOTTOM_PANEL_HEIGHT);
        bottomPanel.setMaxHeight(BOTTOM_PANEL_HEIGHT);
        mainPane.setBottom(bottomPanel);
        subscribeToGameEvents();
        notificationContainer = new VBox(NOTIFICATION_SPACING);
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(10));
        notificationContainer.setMouseTransparent(true);

        StackPane glassRoot = new StackPane();
        glassRoot.getChildren().add(mainPane);
        StackPane.setAlignment(notificationContainer, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationContainer, new Insets(10, 20, 0, 0));
        glassRoot.getChildren().add(notificationContainer);
        glassRoot.setStyle("-fx-font-family: 'Microsoft YaHei';");
        scene = new Scene(glassRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private void subscribeToGameEvents() {
        game.engine.EventBus eventBus = game.engine.EventBus.getInstance();
        eventBus.subscribe("GameOver", event -> {
            IPlayer winner = (IPlayer) event.getData();
            final String message = "🎉 游戏结束！\n" +
                    (winner != null ? "恭喜 " + winner.getName() + " 获得胜利！" : "");

            javafx.application.Platform.runLater(() -> {
                showMessage(message, false);
            });
        });
    }

    private VBox createUserInputPanel() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #ffffff; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        inputPromptLabel = new Label();
        inputPromptLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        inputPromptLabel.setWrapText(true);
        inputPromptLabel.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().add(inputPromptLabel);

        return panel;
    }

    private VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));
        HBox infoBox = new HBox(30);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        turnLabel = new Label("回合数: 0");
        turnLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        currentPlayerLabel = new Label("当前玩家: -");
        currentPlayerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        infoBox.getChildren().addAll(turnLabel, currentPlayerLabel);

        topPanel.getChildren().addAll(infoBox);
        return topPanel;
    }

    private VBox createPlayerInfoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle(
                "-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");

        Label titleLabel = new Label("玩家信息");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Separator separator = new Separator();

        panel.getChildren().addAll(titleLabel, separator);
        return panel;
    }

    private VBox createBottomPanel() {
        VBox bottomPanel = new VBox(5);
        bottomPanel.setPadding(new Insets(10));

        Label messageLabel = new Label("游戏消息:");
        messageLabel.setStyle("-fx-font-weight: bold;");

        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-control-inner-background: #ecf0f1; -fx-font-family: 'Microsoft YaHei', 'SimSun';");
        VBox.setVgrow(messageArea, Priority.ALWAYS);

        bottomPanel.getChildren().addAll(messageLabel, messageArea);
        return bottomPanel;
    }

    public void update(IGameContext gameContext) {
        if (gameContext == null) {
            return;
        }

        turnLabel.setText("回合数: " + gameContext.getCurrentTurnNumber());

        IPlayer currentPlayer = gameContext.getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayerLabel.setText("当前玩家: " + currentPlayer.getName() + " ("
                    + gameContext.getMapProvider().getPlayerColorsName()[currentPlayer.getPlayerId()] + ")");
            currentPlayerLabel
                    .setStyle("-fx-font-size: 14px; -fx-text-fill: "
                            + getColorHex(gameContext.getMapProvider().getPlayerColors()[currentPlayer.getPlayerId()])
                            + "; -fx-font-weight: bold;");
        }

        IGameContext context = game.engine.GameContext.getInstance();
        if (context != null) {
            mapRenderer.renderMap(context);
        }
        updatePlayerInfo(gameContext);
    }

    private void updatePlayerInfo(IGameContext gameContext) {
        if (playerInfoPanel.getChildren().size() > 2) {
            playerInfoPanel.getChildren().remove(2, playerInfoPanel.getChildren().size());
        }

        IPlayer[] players = game.engine.GameContext.getInstance().getPlayers();
        if (players == null) {
            return;
        }
        for (IPlayer player : players) {
            if (player == null)
                continue;

            VBox playerBox = new VBox(5);
            playerBox.setPadding(new Insets(10));
            playerBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");

            Label nameLabel = new Label(player.getName() + (player.isWinner() ? " 🏆" : ""));
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: "
                    + getColorHex(gameContext.getMapProvider().getPlayerColors()[player.getPlayerId()]) + ";");

            Label colorLabel = new Label(
                    "颜色: " + gameContext.getMapProvider().getPlayerColorsName()[player.getPlayerId()]);
            colorLabel.setStyle("-fx-font-size: 12px;");

            playerBox.getChildren().addAll(nameLabel, colorLabel);

            IChessman[] chessmen = player.getChessman();
            IBoard board = game.engine.GameContext.getInstance().getBoard();
            for (int i = 0; i < chessmen.length; i++) {
                IChessman chessman = chessmen[i];
                int position = chessman.getPosition();
                String status;

                if (position == -1) {
                    status = "待起飞";
                } else if (board != null && game.engine.GameContext.getInstance().getMapProvider() != null &&
                        position == game.engine.GameContext.getInstance().getMapProvider()
                                .getEndPosition(player.getPlayerId())) {
                    status = "已到达终点";
                } else {
                    status = "位置 " + position;
                }

                Label chessmanLabel = new Label("  棋子 " + i + ": " + status);
                chessmanLabel.setStyle("-fx-font-size: 11px;");
                playerBox.getChildren().add(chessmanLabel);
            }

            playerInfoPanel.getChildren().add(playerBox);
        }
    }

    public void showMessage(String message, boolean isError) {
        messageArea.appendText((isError ? "[错误] " : "") + message + "\n");
        messageArea.setScrollTop(Double.MAX_VALUE);
    }

    public void showSpecialMessage(String message) {
        addNotification(message);
    }

    private void addNotification(String message) {
        javafx.application.Platform.runLater(() -> {
            NotificationItem notification = new NotificationItem(message);
            activeNotifications.add(notification);
            notificationContainer.getChildren().add(notification.getNode());
            notification.show();
            PauseTransition removeDelay = new PauseTransition(Duration.seconds(4));
            removeDelay.setOnFinished(e -> removeNotification(notification));
            removeDelay.play();
        });
    }

    private void removeNotification(NotificationItem notification) {
        javafx.application.Platform.runLater(() -> {
            notification.hide(() -> {
                activeNotifications.remove(notification);
                notificationContainer.getChildren().remove(notification.getNode());
            });
        });
    }

    public void clearMessages() {
        messageArea.clear();
    }

    public void showChoiceDialog(String prompt, List<String> options, java.util.function.Consumer<Integer> callback) {
        userInputPanel.getChildren().clear();
        inputPromptLabel = new Label(prompt);
        inputPromptLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        inputPromptLabel.setWrapText(true);
        userInputPanel.getChildren().add(inputPromptLabel);
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < options.size(); i++) {
            final int index = i;
            Button btn = new Button((i + 1) + ". " + options.get(i));
            btn.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> {
                hideUserInputPanel();
                if (callback != null) {
                    callback.accept(index);
                }
            });
            buttonBox.getChildren().add(btn);
        }

        userInputPanel.getChildren().add(buttonBox);
        userInputPanel.setVisible(true);
        userInputPanel.setManaged(true);
    }

    /**
     * 显示输入对话框（嵌入式面板）
     * 此方法会立即返回，通过回调通知结果
     */
    public void showInputDialog(String prompt, java.util.function.Consumer<String> callback) {
        userInputPanel.getChildren().clear();
        inputPromptLabel = new Label(prompt);
        inputPromptLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        inputPromptLabel.setWrapText(true);
        userInputPanel.getChildren().add(inputPromptLabel);
        TextField inputField = new TextField();
        inputField.setStyle("-fx-font-size: 14px;");
        inputField.setMaxWidth(Double.MAX_VALUE);
        inputField.setPromptText("请输入...");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button okBtn = new Button("确定");
        okBtn.setStyle(
                "-fx-font-size: 14px; -fx-min-width: 100px; -fx-background-color: #3498db; -fx-text-fill: white;");
        okBtn.setOnAction(e -> {
            String input = inputField.getText();
            hideUserInputPanel();
            if (callback != null) {
                callback.accept(input);
            }
        });

        Button cancelBtn = new Button("取消");
        cancelBtn.setStyle("-fx-font-size: 14px; -fx-min-width: 100px;");
        cancelBtn.setOnAction(e -> {
            hideUserInputPanel();
            if (callback != null) {
                callback.accept("");
            }
        });

        buttonBox.getChildren().addAll(okBtn, cancelBtn);

        userInputPanel.getChildren().addAll(inputField, buttonBox);
        userInputPanel.setVisible(true);
        userInputPanel.setManaged(true);
        inputField.requestFocus();
    }

    public void showConfirmDialog(String message, java.util.function.Consumer<Boolean> callback) {
        userInputPanel.getChildren().clear();
        inputPromptLabel = new Label(message);
        inputPromptLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        inputPromptLabel.setWrapText(true);
        userInputPanel.getChildren().add(inputPromptLabel);
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button yesBtn = new Button("是");
        yesBtn.setStyle(
                "-fx-font-size: 14px; -fx-min-width: 50px; -fx-padding: 10; -fx-background-color: #27ae60; -fx-text-fill: white;");
        yesBtn.setOnAction(e -> {
            hideUserInputPanel();
            if (callback != null) {
                callback.accept(true);
            }
        });

        Button noBtn = new Button("否");
        noBtn.setStyle(
                "-fx-font-size: 14px; -fx-min-width: 50px; -fx-padding: 10; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        noBtn.setOnAction(e -> {
            hideUserInputPanel();
            if (callback != null) {
                callback.accept(false);
            }
        });

        buttonBox.getChildren().addAll(yesBtn, noBtn);
        userInputPanel.getChildren().add(buttonBox);
        userInputPanel.setVisible(true);
        userInputPanel.setManaged(true);
    }

    private void hideUserInputPanel() {
        userInputPanel.setVisible(false);
        userInputPanel.setManaged(false);
    }

    public boolean confirmExit() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        alert.setContentText("确定要退出游戏吗？");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
    }

    private String getColorHex(Colors color) {
        return "#000000";
    }

    public Scene getScene() {
        return scene;
    }

    public MapRenderer getMapRenderer() {
        return mapRenderer;
    }

    private static class NotificationItem {
        private VBox node;
        private Label messageLabel;
        private String message;

        public NotificationItem(String message) {
            this.message = message;
            createNode();
        }

        private void createNode() {
            node = new VBox(5);
            node.setAlignment(Pos.CENTER);
            node.setPadding(new Insets(15, 20, 15, 20));
            node.setMaxWidth(400);
            node.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.2); " +
                            "-fx-background-radius: 10; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 10; ");

            messageLabel = new Label(message);
            messageLabel.setStyle(
                    "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: white; " +
                            "-fx-wrap-text: true;");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(360);

            node.getChildren().add(messageLabel);

            node.setOpacity(0);
            node.setTranslateX(50);
        }

        public void show() {
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), node);
            slideIn.setFromX(50);
            slideIn.setToX(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ParallelTransition showTransition = new ParallelTransition(slideIn, fadeIn);
            showTransition.play();
        }

        public void hide(Runnable onComplete) {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), node);
            slideOut.setFromX(0);
            slideOut.setToX(50);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), node);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            ParallelTransition hideTransition = new ParallelTransition(slideOut, fadeOut);
            hideTransition.setOnFinished(e -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            });
            hideTransition.play();
        }

        public VBox getNode() {
            return node;
        }

    }
}
