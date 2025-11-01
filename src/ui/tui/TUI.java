package ui.tui;

import ui.api.IUserInterface;
import game.api.*;
import java.util.List;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class TUI implements IUserInterface {
    private Scanner scanner;
    private Renderer renderer;
    private IGameContext gameContext;
    private boolean subscribedEvent = false;

    public TUI() {
        try {
            this.scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            this.scanner = new Scanner(System.in);
        }
        this.renderer = new Renderer();
        game.engine.AnimationConfig.setMoveDelay(0);
    }

    @Override
    public void initialize(IGameContext context) {
        this.gameContext = context;
        displayMessage("[OK] TUI 初始化完成");

        if (this.subscribedEvent) {
            return;
        }
        this.subscribedEvent = true;

        subscribeToBlockEffects();
    }

    private void subscribeToBlockEffects() {
        game.engine.EventBus eventBus = game.engine.EventBus.getInstance();

        eventBus.subscribe("BlockEffect", event -> {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> data = (java.util.Map<String, Object>) event.getData();
            if (data != null) {
                String message = (String) data.get("message");
                if (message != null) {
                    displayMessage("\n" + "!".repeat(60));
                    displayMessage("🎯 特殊方块触发！");
                    displayMessage(message);
                    displayMessage("!".repeat(60) + "\n");
                }
            }
        });

        eventBus.subscribe("GameOver", event -> {
            IPlayer winner = (IPlayer) event.getData();
            displayMessage("\n" + "=".repeat(60));
            displayMessage("🎉 游戏结束！");
            if (winner != null) {
                displayMessage("恭喜 " + winner.getName() + " 获得胜利！");
            }
            displayMessage("=".repeat(60) + "\n");
        });
    }

    @Override
    public void show() {
        displayMessage("\n" + "=".repeat(60));
        displayMessage("                 欢迎来到飞行棋游戏！");
        displayMessage("=".repeat(60) + "\n");
    }

    @Override
    public void hide() {
        displayMessage("\n游戏界面已隐藏");
    }

    @Override
    public void setContext(IGameContext gameContext) {
        this.gameContext = gameContext;
    }

    @Override
    public void update() {
        if (gameContext == null) {
            displayError("游戏状态未初始化，无法更新显示");
            return;
        }

        renderer.renderMap(gameContext);
        IGameContext context = game.engine.GameContext.getInstance();
        if (context != null) {
            renderer.renderMap(context);
            IPlayer[] players = context.getPlayers();
            renderer.renderPlayers(players);
        }

    }

    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void displayError(String error) {
        System.err.println("[ERROR] 错误: " + error);
    }

    @Override
    public int getUserChoice(String prompt, List<String> options) {
        displayMessage("\n" + prompt);
        for (int i = 0; i < options.size(); i++) {
            displayMessage("  [" + i + "] " + options.get(i));
        }

        while (true) {
            System.out.print("请选择 (0-" + (options.size() - 1) + "): ");
            try {
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    displayError("输入不能为空，请输入 0-" + (options.size() - 1) + " 之间的数字");
                    continue;
                }

                int choice = Integer.parseInt(input);

                if (choice >= 0 && choice < options.size()) {
                    return choice;
                }
                displayError("无效的选择，请输入 0-" + (options.size() - 1) + " 之间的数字");
            } catch (NumberFormatException e) {
                displayError("输入无效，请输入数字而不是字符或其他内容");
            }
        }
    }

    @Override
    public String getUserInput(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    @Override
    public boolean confirm(String message) {
        System.out.print(message + " (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }

    @Override
    public void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    @Override
    public void close() {
        displayMessage("\n感谢游玩！再见！");
        if (scanner != null) {
            scanner.close();
        }
    }
}