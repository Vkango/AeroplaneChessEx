package bootstrap;

import bootstrap.spi.PluginLoader;
import game.api.IGameEngine;
import game.engine.GameEngine;
import plugin.api.IRuleSetProvider;
import plugin.api.IMapProvider;
import ui.api.IUserInterface;
import ui.tui.TUI;
import ui.util.ColorFactory;
import ui.gui.GUI;

public class ServiceLoader {

    /**
     * 加载GameEngine实现
     * 
     * @return GameEngine实例
     */
    public static IGameEngine loadGameEngine() {
        return new GameEngine();
    }

    /**
     * 
     * @param name 规则集名称
     * @return RuleSetProvider实例
     * @throws RuntimeException 如果规则集不存在
     */
    public static IRuleSetProvider loadRuleSet(String name) {
        return PluginLoader.getInstance().getRuleSetProvider(name);
    }

    /**
     * 
     * @param name 地图名称
     * @return MapProvider实例
     * @throws RuntimeException 如果地图不存在
     */
    public static IMapProvider loadMapProvider(String name) {
        return PluginLoader.getInstance().getMapProvider(name);
    }

    /**
     * 加载UI
     * 
     * @param type UI类型 (tui/gui)
     * @return UI实例
     * @throws RuntimeException 如果UI类型不支持
     */
    public static IUserInterface loadUI(String type) {
        if ("tui".equalsIgnoreCase(type)) {
            ColorFactory.set(ColorFactory.TUI);
            return new TUI();
        } else if ("gui".equalsIgnoreCase(type)) {
            ColorFactory.set(ColorFactory.GUI);
            GUI.launchGUI();
            return GUI.getInstance();
        }
        throw new RuntimeException("Unknown UI type: " + type);
    }

    /**
     * 创建玩家列表
     * 
     * @param ui      UI界面
     * @param ruleSet 规则集
     * @return 玩家数组
     */
    public static game.api.IPlayer[] createPlayers(ui.api.IUserInterface ui, IRuleSetProvider ruleSet,
            IMapProvider mapProvider) {
        int minPlayers = mapProvider.getMinPlayers();
        int maxPlayers = mapProvider.getMaxPlayers();

        ui.displayMessage("游戏需要 " + minPlayers + "-" + maxPlayers + " 名玩家");

        int playerCount = 0;
        while (playerCount < minPlayers || playerCount > maxPlayers) {
            String input = ui.getUserInput("请输入玩家数量 (" + minPlayers + "-" + maxPlayers + ")");
            try {
                playerCount = Integer.parseInt(input);
                if (playerCount < minPlayers || playerCount > maxPlayers) {
                    ui.displayError("玩家数量必须在 " + minPlayers + "-" + maxPlayers + " 之间");
                }
            } catch (NumberFormatException e) {
                ui.displayError("请输入有效的数字");
            }
        }

        int maxChessmanPerPlayer = mapProvider.getMaxChessmanPerPlayer();
        int chessmanCount = 0;
        while (chessmanCount < 1 || chessmanCount > maxChessmanPerPlayer) {
            String input = ui.getUserInput("每个玩家的棋子数量 (" + 1 + "-" + maxChessmanPerPlayer + ")");
            try {
                chessmanCount = Integer.parseInt(input);
                if (chessmanCount < 1 || chessmanCount > maxChessmanPerPlayer) {
                    ui.displayError("每个玩家的棋子数量必须在 " + 1 + "-" + maxChessmanPerPlayer + " 之间");
                }
            } catch (NumberFormatException e) {
                ui.displayError("请输入有效的数字");
            }
        }

        game.api.IPlayer[] players = new game.api.IPlayer[playerCount];
        String[] colorName = mapProvider.getPlayerColorsName();

        for (int i = 0; i < playerCount; i++) {
            String name = ui.getUserInput("请输入玩家 " + (i + 1) + " 的名字");
            if (name.isEmpty()) {
                name = "玩家" + (i + 1);
            }
            String color = colorName[i % colorName.length];
            players[i] = new game.engine.Player(name, i, chessmanCount);
            ui.displayMessage("[OK] 玩家 " + name + " (" + color + ") 已加入游戏");
        }

        return players;
    }

    /**
     * 创建游戏棋盘
     * 
     * @param mapProvider 地图提供者
     * @param players     玩家数组
     * @param ruleSet     规则集
     * @return 棋盘实例
     */
    public static game.api.IBoard createBoard() {
        return new game.engine.Board();
    }
}
