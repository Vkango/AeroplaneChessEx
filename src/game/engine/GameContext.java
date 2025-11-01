package game.engine;

import game.api.*;
import plugin.api.IMapProvider;
import plugin.api.IRuleSetProvider;
import ui.api.IUserInterface;
import game.api.IGameContext;

public final class GameContext implements IGameContext {
    private static final GameContext INSTANCE = new GameContext();
    public final GameState state = new GameState();
    private IMapProvider mapProvider;
    private IRuleSetProvider ruleSetProvider;
    private IUserInterface userInterface;
    private IPlayer[] players;
    private IBoard board;
    private IEventBus eventBus = EventBus.getInstance();

    private GameContext() {
    }

    public static GameContext getInstance() {
        return INSTANCE;
    }

    @Override
    public IMapProvider getMapProvider() {
        return mapProvider;
    }

    @Override
    public void setMapProvider(IMapProvider mapProvider) {
        this.mapProvider = mapProvider;
    }

    @Override
    public IRuleSetProvider getRuleSetProvider() {
        return ruleSetProvider;
    }

    @Override
    public void setRuleSetProvider(IRuleSetProvider ruleSetProvider) {
        this.ruleSetProvider = ruleSetProvider;
    }

    @Override
    public IUserInterface getUserInterface() {
        return userInterface;
    }

    @Override
    public void setUserInterface(IUserInterface userInterface) {
        this.userInterface = userInterface;
    }

    @Override
    public IPlayer[] getPlayers() {
        return players;
    }

    @Override
    public void setPlayers(IPlayer[] players) {
        this.players = players;
    }

    @Override
    public IBoard getBoard() {
        return board;
    }

    @Override
    public void setBoard(IBoard board) {
        this.board = board;
    }

    @Override
    public IEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public IGameState getState() {
        return state;
    }

    @Override
    public IPlayer getCurrentPlayer() {
        return state.getCurrentPlayer();
    }

    @Override
    public int getCurrentTurnNumber() {
        return state.getTurnNumber();
    }

    @Override
    public int getCurrentDiceValue() {
        return state.getCurrentDiceValue();
    }

    @Override
    public void reset() {
        state.reset();
    }

    @Override
    public void clear() {
        this.mapProvider = null;
        this.ruleSetProvider = null;
        this.userInterface = null;
        this.players = null;
        this.board = null;
        reset();
    }
}
