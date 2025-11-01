package ui.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.api.IUserInterface;
import game.api.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GUI extends Application implements IUserInterface {
    private static GUI instance;
    private static CountDownLatch latch = new CountDownLatch(1);

    private IGameContext gameContext;
    private Stage primaryStage;
    private GameWindow gameWindow;

    private volatile Object userResponse;
    private volatile CountDownLatch responseLatch;

    public GUI() {
        instance = this;
        game.engine.AnimationConfig.setMoveDelay(100);
    }

    public static GUI getInstance() {
        return instance;
    }

    public static void launchGUI() {
        new Thread(() -> {
            Application.launch(GUI.class);
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("AeroplaneChessEx");
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            if (gameWindow == null) {
                Platform.exit();
                System.exit(0);
                return;
            }
            if (gameWindow.confirmExit()) {
                Platform.exit();
                System.exit(0);
            }
        });

        latch.countDown();
    }

    @Override
    public void initialize(IGameContext context) {
        CountDownLatch initLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                System.out.println("[DEBUG] 开始创建GameWindow...");
                gameWindow = new GameWindow();
                System.out.println("[DEBUG] GameWindow创建完成");

                primaryStage.setScene(gameWindow.getScene());
                System.out.println("[DEBUG] Scene已设置");

                primaryStage.setResizable(false);
                primaryStage.sizeToScene();

                System.out.println("[DEBUG] 窗口配置完成");
                gameWindow.showMessage("GUI 初始化完成", false);
                subscribeToGameEvents();
            } catch (Exception e) {
                System.err.println("[ERROR] GameWindow初始化失败: " + e.getMessage());
                e.printStackTrace();
            } finally {
                initLatch.countDown();
            }
        });

        try {
            initLatch.await();
            System.out.println("[DEBUG] initialize() 完成");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToGameEvents() {
        IEventBus eventBus = game.engine.EventBus.getInstance();
        eventBus.subscribe("ChessmanMoveEasing", event -> {
            java.util.Map<String, Object> data = event.getData();
            if (data != null) {
                IChessman chessman = (IChessman) data.get("chessman");
                Integer fromPos = (Integer) data.get("from");
                Integer toPos = (Integer) data.get("to");

                if (chessman != null && fromPos != null && toPos != null) {
                    java.util.List<Integer> path = generatePath(fromPos, toPos);
                    String chessmanId = chessman.getOwner().getName() + "_" + chessman.getChessmanId();
                    Platform.runLater(() -> {
                        if (gameWindow != null) {
                            gameWindow.getMapRenderer().animateChessmanMove(chessmanId, fromPos, toPos, path);
                        }
                    });
                }
            }
        });
        eventBus.subscribe("BlockEffect", event -> {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) event.getData();
            if (data != null) {
                String message = (String) data.get("message");
                if (message != null) {
                    Platform.runLater(() -> {
                        if (gameWindow != null) {
                            gameWindow.showSpecialMessage(message);
                        }
                    });
                }
            }
        });
    }

    private java.util.List<Integer> generatePath(int from, int to) {
        java.util.List<Integer> path = new java.util.ArrayList<>();
        if (from < to) {
            for (int i = from; i <= to; i++) {
                path.add(i);
            }
        } else {
            for (int i = from; i >= to; i--) {
                path.add(i);
            }
        }

        return path;
    }

    @Override
    public void show() {
        CountDownLatch showLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                System.out.println("[DEBUG] show() 被调用");
                if (primaryStage.getScene() == null) {
                    System.out.println("[WARNING] Scene尚未设置，延迟显示");
                }
                if (!primaryStage.isShowing()) {
                    primaryStage.show();
                    System.out.println("[DEBUG] 窗口已显示");
                }
                primaryStage.toFront();
            } finally {
                showLatch.countDown();
            }
        });
        try {
            showLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hide() {
        Platform.runLater(() -> {
            primaryStage.hide();
        });
    }

    @Override
    public void setContext(IGameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            if (gameWindow != null) {
                gameWindow.update(this.gameContext);
            }
        });
    }

    @Override
    public void displayMessage(String message) {
        if (gameWindow == null) {
            System.out.println(message);
            return;
        }
        Platform.runLater(() -> {
            if (gameWindow != null) {
                gameWindow.showMessage(message, false);
            }
        });
    }

    @Override
    public void displayError(String error) {
        gameWindow.showSpecialMessage(error);
    }

    @Override
    public int getUserChoice(String prompt, List<String> options) {
        responseLatch = new CountDownLatch(1);
        userResponse = null;

        Platform.runLater(() -> {
            if (gameWindow != null) {
                gameWindow.showChoiceDialog(prompt, options, choice -> {
                    userResponse = choice;
                    responseLatch.countDown();
                });
            }
        });

        try {
            responseLatch.await();
            return (Integer) userResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public String getUserInput(String prompt) {
        responseLatch = new CountDownLatch(1);
        userResponse = null;

        Platform.runLater(() -> {
            if (gameWindow != null) {
                gameWindow.showInputDialog(prompt, input -> {
                    userResponse = input;
                    responseLatch.countDown();
                });
            }
        });

        try {
            responseLatch.await();
            return (String) userResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean confirm(String message) {
        responseLatch = new CountDownLatch(1);
        userResponse = null;

        Platform.runLater(() -> {
            if (gameWindow != null) {
                gameWindow.showConfirmDialog(message, result -> {
                    userResponse = result;
                    responseLatch.countDown();
                });
            }
        });

        try {
            responseLatch.await();
            return (Boolean) userResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void clear() {
        Platform.runLater(() -> {
            if (gameWindow != null) {
                gameWindow.clearMessages();
            }
        });
    }

    @Override
    public void close() {
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
            Platform.exit();
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
