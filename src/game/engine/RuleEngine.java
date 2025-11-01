package game.engine;

import game.api.*;
import plugin.api.IRuleSetProvider;
import plugin.api.IMapProvider;
import ui.api.IUserInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleEngine {
    private GameContext context = GameContext.getInstance();
    private IEventBus eventBus = EventBus.getInstance();

    public RuleEngine() {
        eventBus.subscribe("DiceRolled", this::onDiceRolled);
        eventBus.subscribe("ChessmanMoved", this::checkGameOver);
        eventBus.subscribe("ChessmanOverEnd", this::onChessmanOverEnd);
    }

    private void onChessmanOverEnd(IGameEvent event) {
        if (event == null || !(event.getData() instanceof Map<?, ?>)) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) event.getData();
        Chessman chessman = (Chessman) data.get("chessman");
        int endPosition = (int) data.get("endPosition");
        int overSteps = (int) data.get("overSteps");

        IRuleSetProvider ruleSetProvider = context.getRuleSetProvider();
        int finalPosition = endPosition
                + ruleSetProvider.getOverEndRule().handleOverEnd(chessman, overSteps, context);

        System.out.println("[RuleEngine] 棋子 " + chessman.getChessmanId() +
                " 越过终点 " + overSteps + " 格，规则将其更新为 " + finalPosition);

        chessman.setPosition(finalPosition);
    }

    private void onDiceRolled(IGameEvent event) {
        if (event == null || event.getData() == null) {
            return;
        }

        IUserInterface userInterface = context.getUserInterface();
        if (userInterface == null) {
            System.err.println("[RuleEngine] 错误：用户界面未初始化");
            return;
        }

        if (!(event.getData() instanceof Map<?, ?>)) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) event.getData();
        int diceValue = (Integer) data.get("diceValue");
        IPlayer player = (IPlayer) data.get("player");

        ActionOptions options = analyzeActions(player, diceValue);
        if (options.isEmpty()) {
            userInterface.displayMessage("[NOTICE] 玩家 " + player.getName() + " 摇到了 " + diceValue + "，但没有可用的操作！");
            return;
        }

        int choice = userInterface.getUserChoice(
                "玩家 " + player.getName() + " 摇到了 " + diceValue + "，请选择您的操作:",
                options.getOptionDescriptions());

        while (choice < 0 || choice >= options.size()) {
            userInterface.displayError("无效选择，请重新选择！");
            choice = userInterface.getUserChoice(
                    "玩家 " + player.getName() + " 摇到了 " + diceValue + "，请选择您的操作:",
                    options.getOptionDescriptions());
        }

        executeAction(player, diceValue, options, choice);
    }

    private ActionOptions analyzeActions(IPlayer player, int diceValue) {
        ActionOptions options = new ActionOptions();
        IRuleSetProvider ruleSetProvider = context.getRuleSetProvider();
        IMapProvider mapProvider = context.getMapProvider();
        int endPosition = mapProvider.getEndPosition(player.getPlayerId());
        for (int i = 0; i < player.getChessman().length; i++) {
            IChessman chessman = player.getChessman()[i];
            int position = chessman.getPosition();

            // 只有已经在地图上的棋子才能移动（position >= 0），并且未到达终点
            if (position >= 0 && position < endPosition &&
                    ruleSetProvider.getMoveStrategy().canMove(chessman, diceValue, context)) {
                options.addMoveOption(i, position);
            }
        }

        // 检查可以起飞的棋子
        if (ruleSetProvider.getTakeOffCondition().canTakeOff(diceValue)) {
            for (int i = 0; i < player.getChessman().length; i++) {
                IChessman chessman = player.getChessman()[i];
                // 棋子位置为-1表示在待起飞区
                if (chessman.getPosition() == -1) {
                    options.addTakeOffOption(i);
                }
            }
        }

        return options;
    }

    /**
     * 执行玩家选择的操作
     */
    private void executeAction(IPlayer player, int diceValue, ActionOptions options, int choice) {
        IBoard board = context.getBoard();
        IUserInterface userInterface = context.getUserInterface();

        if (options.isMoveAction(choice)) {
            // 移动棋子
            int chessmanId = options.getChessmanId(choice);
            IChessman chessman = player.getChessman()[chessmanId];
            int oldPosition = chessman.getPosition();

            board.moveChessman(chessman, diceValue);
            userInterface.displayMessage("[OK] 玩家 " + player.getName() + " 将棋子 " + chessmanId +
                    " 从位置 " + oldPosition + " 移动到位置 " + chessman.getPosition());

        } else {
            // 起飞棋子
            int chessmanId = options.getChessmanId(choice);
            IMapProvider mapProvider = context.getMapProvider();
            int startPosition = mapProvider.getStartPosition(player.getPlayerId());
            player.getChessman()[chessmanId].setPosition(startPosition);

            userInterface.displayMessage("[OK] 玩家 " + player.getName() + " 的棋子 " + chessmanId +
                    " 起飞到位置 " + startPosition + "！");

            // 发布棋子起飞事件（起飞触发起点方块效果）
            eventBus.publish(new GameEvent("ChessmanTakeOff", player.getChessman()[chessmanId],
                    "棋子 " + chessmanId + " 起飞到位置 " + startPosition));
        }
    }

    private static class ActionOptions {
        private List<String> descriptions = new ArrayList<>();
        private List<Integer> chessmanIds = new ArrayList<>();
        private List<Boolean> isMoveAction = new ArrayList<>();

        void addMoveOption(int chessmanId, int currentPosition) {
            descriptions.add("移动棋子 " + chessmanId + " (当前位置: " + currentPosition + ")");
            chessmanIds.add(chessmanId);
            isMoveAction.add(true);
        }

        void addTakeOffOption(int chessmanId) {
            descriptions.add("起飞棋子 " + chessmanId);
            chessmanIds.add(chessmanId);
            isMoveAction.add(false);
        }

        boolean isEmpty() {
            return descriptions.isEmpty();
        }

        int size() {
            return descriptions.size();
        }

        List<String> getOptionDescriptions() {
            return descriptions;
        }

        int getChessmanId(int choice) {
            return chessmanIds.get(choice);
        }

        boolean isMoveAction(int choice) {
            return isMoveAction.get(choice);
        }
    }

    private void checkGameOver(IGameEvent event) {
        // 游戏已经结束，不再检查
        if (context.state.isGameOver()) {
            return;
        }

        IRuleSetProvider ruleSetProvider = context.getRuleSetProvider();
        IPlayer[] players = context.getPlayers();

        if (ruleSetProvider == null || players == null) {
            return;
        }

        if (ruleSetProvider.isGameOver(players)) {
            IPlayer winner = ruleSetProvider.getWinner(players);
            context.state.setWinner(winner);
            context.state.setGameOver(true);

            eventBus.publish(new GameEvent("GameOver", winner,
                    "游戏结束，获胜者：" + (winner != null ? winner.getName() : "无")));
        }
    }
}
