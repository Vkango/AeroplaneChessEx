package game.engine;

import game.api.*;
import plugin.api.IRuleSetProvider;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import ui.api.IUserInterface;

public class GameEngine implements IGameEngine {
    private final GameContext context = GameContext.getInstance();
    private final IEventBus eventBus = EventBus.getInstance();
    private boolean mapGrantExtraTurn = false;
    @SuppressWarnings("unused")
    private RuleEngine ruleEngine;

    public void setRuleSetProvider(IRuleSetProvider ruleSetProvider) {
        context.setRuleSetProvider(ruleSetProvider);
    }

    public void setBoard(IBoard board) {
        context.setBoard(board);
    }

    public void setUserInterface(IUserInterface userInterface) {
        context.setUserInterface(userInterface);
    }

    @Override
    public void initialize(List<IPlayer> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("玩家列表不能为空");
        }

        if (context.state.isRunning()) {
            stop();
            System.out.println("[INFO] 停止旧游戏以重新初始化");
        }

        IBoard board = context.getBoard();
        if (board != null && board instanceof Board) {
            ((Board) board).reset();
            System.out.println("[INFO] 已重置棋盘状态");
        }

        context.setPlayers(players.toArray(new IPlayer[0]));

        context.reset();

        this.ruleEngine = new RuleEngine();

        eventBus.subscribe("EndTurn", this::onEndTurn);
        eventBus.subscribe("GrantNewTurn", this::onMapGrantExtraTurn);

        eventBus.publish(new GameEvent("GameInitialized", players, "游戏初始化完成"));
    }

    private void onMapGrantExtraTurn(IGameEvent event) {
        this.mapGrantExtraTurn = true;
    }

    @Override
    public void start() {
        if (context.state.isRunning()) {
            System.out.println("游戏已经在运行中");
            return;
        }

        context.state.setRunning(true);

        eventBus.publish(new GameEvent("GameStarted", null, "游戏开始"));

        System.out.println("游戏开始！");
    }

    @Override
    public void stop() {
        if (!context.state.isRunning()) {
            return;
        }

        context.state.setRunning(false);

        eventBus.publish(new GameEvent("GameStopped", null, "游戏停止"));
    }

    @Override
    public void pause() {
        if (!context.state.isRunning()) {
            return;
        }

        context.state.setPaused(true);

        eventBus.publish(new GameEvent("GamePaused", null, "游戏暂停"));

        System.out.println("游戏已暂停");
    }

    @Override
    public void resume() {
        if (!context.state.isRunning() || !context.state.isPaused()) {
            return;
        }

        context.state.setPaused(false);

        eventBus.publish(new GameEvent("GameResumed", null, "游戏恢复"));

    }

    @Override
    public void playTurn() {
        if (!context.state.isRunning() || context.state.isPaused() || context.state.isGameOver()) {
            return;
        }

        turnStart();

        Map<String, Object> endTurnData = new HashMap<>();
        endTurnData.put("diceValue", context.state.getCurrentDiceValue());
        eventBus.publish(new GameEvent("EndTurn", endTurnData, "回合结束信号"));
    }

    private void turnStart() {
        IPlayer[] players = context.getPlayers();
        if (players == null || players.length == 0) {
            return;
        }

        if (!context.state.hasExtraTurn()) {
            int currentIndex = context.state.getCurrentPlayerIndex();
            currentIndex = (currentIndex + 1) % players.length;
            context.state.setCurrentPlayerIndex(currentIndex);

            if (currentIndex == 0) {
                context.state.setTurnNumber(context.state.getTurnNumber() + 1);
            }
        }
        context.state.setExtraTurn(false);

        IPlayer currentPlayer = players[context.state.getCurrentPlayerIndex()];
        context.state.setCurrentPlayer(currentPlayer);

        eventBus.publish(new GameEvent("TurnStarted", currentPlayer,
                "玩家 " + currentPlayer.getName() + " 的回合开始"));

        int diceValue = Dice.roll();
        context.state.setCurrentDiceValue(diceValue);

        Map<String, Object> diceData = new HashMap<>();
        diceData.put("player", currentPlayer);
        diceData.put("diceValue", diceValue);

        eventBus.publish(new GameEvent("DiceRolled", diceData,
                "玩家 " + currentPlayer.getName() + " 掷出 " + diceValue + " 点"));
    }

    private void onEndTurn(IGameEvent event) {
        IPlayer currentPlayer = context.state.getCurrentPlayer();
        if (currentPlayer == null) {
            return;
        }

        if (event.getData() instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> eventData = (Map<String, Object>) event.getData();
            int diceValue = (int) eventData.get("diceValue");
            IRuleSetProvider ruleSetProvider = context.getRuleSetProvider();
            if ((ruleSetProvider != null && ruleSetProvider.shouldGrantExtraTurn(diceValue))
                    || this.mapGrantExtraTurn) {
                context.state.setExtraTurn(true);
                this.mapGrantExtraTurn = false;
            }

        }

        Map<String, Object> turnEndData = new HashMap<>();
        turnEndData.put("player", currentPlayer);
        turnEndData.put("diceValue", context.state.getCurrentDiceValue());
        turnEndData.put("hasExtraTurn", context.state.hasExtraTurn());

        eventBus.publish(new GameEvent("TurnEnded", turnEndData,
                "玩家 " + currentPlayer.getName() + " 的回合结束" +
                        (context.state.hasExtraTurn() ? "（获得额外回合）" : "")));
    }
}
