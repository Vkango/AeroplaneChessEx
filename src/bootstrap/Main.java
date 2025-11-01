package bootstrap;

import game.api.IGameEngine;
import game.api.IPlayer;
import game.engine.EventBus;
import game.engine.GameContext;
import plugin.api.IRuleSetProvider;
import plugin.api.IMapProvider;
import ui.api.IUserInterface;
import java.util.List;

public class Main {
    private static final String VERSION = "1.0.0.re";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      AeroplaneChessEx v" + VERSION);
        System.out.println("========================================\n");

        try {
            GameConfig config = parseArgs(args);
            System.out.println("正在加载插件...\n");
            bootstrap.spi.PluginLoader.getInstance().loadPluginsFromJars(config.getPluginJars());
            System.out.println();
            start(config);

        } catch (Exception e) {
            System.err.println("[ERROR] Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runGameLoop(IGameEngine engine, IUserInterface ui) {
        engine.start();
        game.engine.GameContext context = game.engine.GameContext.getInstance();
        ui.setContext(context);
        ui.displayMessage("游戏开始！\n");

        // 主循环：持续执行回合直到游戏结束
        while (context.state.isRunning() && !context.state.isGameOver()) {
            engine.playTurn();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[INFO] 游戏循环被中断，准备重启...");
                break;
            }
        }

        if (context.state.isRunning()) {
            engine.stop();
        }

        ui.update();

        if (context.state.isGameOver()) {
            game.api.IPlayer winner = context.state.getWinner();
            if (winner != null) {
                String victoryMessage = "\n游戏结束！\n\n" +
                        "恭喜 " + winner.getName() + " ("
                        + context.getMapProvider().getPlayerColorsName()[winner.getPlayerId()] + ") 获得胜利！\n";
                ui.displayMessage(victoryMessage);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static void start(GameConfig config) {
        System.out.println("以进入游戏");
        IUserInterface ui = ServiceLoader.loadUI(config.getUiType());
        System.out.println("[OK] UI 初始化完成: " + config.getUiType());
        if ("gui".equalsIgnoreCase(config.getUiType())) {
            final IUserInterface finalUi = ui;
            Thread gameThread = new Thread(() -> {
                runGameLogic(finalUi, config);
            }, "GameLogicThread");
            gameThread.setDaemon(false);
            gameThread.start();
            return;
        }

        runGameLogic(ui, config);
    }

    private static void runGameLogic(IUserInterface ui, GameConfig config) {

        System.out.println("正在加载规则和地图...\n");
        IRuleSetProvider ruleSet = ServiceLoader.loadRuleSet(config.getRuleSetName());
        IMapProvider mapProvider = ServiceLoader.loadMapProvider(config.getMapName());
        System.out.println("[OK] 已加载规则集: " + ruleSet.getRuleSetName());
        System.out.println("[OK] 已加载地图: " + mapProvider.getMapName() + "\n");
        IGameEngine engine = ServiceLoader.loadGameEngine();
        ui.initialize(game.engine.GameContext.getInstance());
        System.out.println("[DEBUG] UI初始化完成");
        ui.show();
        System.out.println("[DEBUG] 窗口已显示");
        ui.displayMessage("正在加载规则和地图...\n");
        ui.displayMessage("[OK] 已加载规则集: " + ruleSet.getRuleSetName());
        ui.displayMessage("[OK] 已加载地图: " + mapProvider.getMapName() + "\n");
        game.api.IPlayer[] playerArray = ServiceLoader.createPlayers(ui, ruleSet, mapProvider);
        List<IPlayer> players = java.util.Arrays.asList(playerArray);
        game.engine.GameEngine gameEngine = (game.engine.GameEngine) engine;
        GameContext.getInstance().setUserInterface(ui);
        gameEngine.setRuleSetProvider(ruleSet);
        gameEngine.setUserInterface(ui);
        GameContext.getInstance().setMapProvider(mapProvider);
        GameContext.getInstance().setPlayers(playerArray);
        GameContext.getInstance().setRuleSetProvider(ruleSet);
        game.api.IBoard board = ServiceLoader.createBoard();
        gameEngine.setBoard(board);
        GameContext.getInstance().setBoard(board);
        EventBus.getInstance().subscribe("ChessmanMoved", event -> {
            ui.update();
        });

        ui.displayMessage("[OK] 游戏引擎配置完成\n");
        engine.initialize(players);
        ui.displayMessage("[OK]  已初始化 " + players.size() + " 名玩家\n");

        ui.displayMessage("========================================");
        ui.displayMessage("    [OK] 游戏准备就绪！开始游戏...");
        ui.displayMessage("========================================\n");
        ui.setContext(game.engine.GameContext.getInstance());
        ui.update();
        System.out.println("[DEBUG] 初始地图已渲染");
        boolean playAgain = true;
        // 再玩一局
        while (playAgain) {
            runGameLoop(engine, ui);
            ui.displayMessage("\n========================================");
            ui.displayMessage("           [NOTICE] 游戏结束！");
            ui.displayMessage("========================================\n");

            playAgain = ui.confirm("再来一局？");

            if (playAgain) {
                ui.displayMessage("\n[INFO] 重新开始游戏...\n");
                game.api.IPlayer[] newPlayerArray = ServiceLoader.createPlayers(ui, ruleSet, mapProvider);
                List<IPlayer> newPlayers = java.util.Arrays.asList(newPlayerArray);
                engine.initialize(newPlayers);
                ui.update();
                ui.displayMessage("========================================");
                ui.displayMessage("    [OK] 游戏准备就绪！开始游戏...");
                ui.displayMessage("========================================\n");
                players = newPlayers;
                playerArray = newPlayerArray;
            }
        }

        ui.displayMessage("\n[INFO] 感谢游玩！\n");
        ui.close();
    }

    private static GameConfig parseArgs(String[] args) {
        GameConfig config = new GameConfig();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "--map":
                case "-m":
                    if (i + 1 < args.length) {
                        config.setMapName(args[++i]);
                    }
                    break;

                case "--ruleset":
                case "-r":
                    if (i + 1 < args.length) {
                        config.setRuleSetName(args[++i]);
                    }
                    break;

                case "--ui":
                    if (i + 1 < args.length) {
                        config.setUiType(args[++i]);
                    }
                    break;

                case "--plugin":
                case "-p":
                    if (i + 1 < args.length) {
                        config.addPluginJar(args[++i]);
                    }
                    break;

                case "--help":
                    printHelp();
                    System.exit(0);
                    break;

                default:
                    System.err.println("[ERROR] 未知的参数: " + arg);
                    printHelp();
                    System.exit(1);
            }
        }

        return config;
    }

    private static void printHelp() {
        System.out.println("Usage: java bootstrap.Main [options]");
        System.out.println("\nOptions:");
        System.out.println("  --map, -m <name>      地图名称 (默认: random)");
        System.out.println("  --ruleset, -r <name>  规则集名称 (默认: default)");
        System.out.println("  --ui <type>           用户界面类型: tui 或 gui (默认: gui)");
        System.out.println("  --plugin, -p <path>   插件JAR文件路径 (可多次指定)");
        System.out.println("  --help                显示帮助信息");
        System.out.println("\nExamples:");
        System.out.println("  java bootstrap.Main");
        System.out.println("  java bootstrap.Main --map default --ui gui");
        System.out.println("  java bootstrap.Main --plugin myplugin.jar --map custom");
        System.out.println("  java bootstrap.Main -p plugin1.jar -p plugin2.jar -r custom");
    }

    private static class GameConfig {
        private String mapName = "random";
        private String ruleSetName = "default";
        private String uiType = "gui";
        private java.util.List<String> pluginJars = new java.util.ArrayList<>();

        // Getters and Setters
        public String getMapName() {
            return mapName;
        }

        public void setMapName(String mapName) {
            this.mapName = mapName;
        }

        public String getRuleSetName() {
            return ruleSetName;
        }

        public void setRuleSetName(String ruleSetName) {
            this.ruleSetName = ruleSetName;
        }

        public String getUiType() {
            return this.uiType;
        }

        public void setUiType(String uiType) {
            this.uiType = uiType;
        }

        public java.util.List<String> getPluginJars() {
            return pluginJars;
        }

        public void addPluginJar(String jarPath) {
            this.pluginJars.add(jarPath);
        }

    }
}
