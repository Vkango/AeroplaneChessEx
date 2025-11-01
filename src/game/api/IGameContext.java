package game.api;

import plugin.api.IMapProvider;
import plugin.api.IRuleSetProvider;
import ui.api.IUserInterface;

public interface IGameContext {
    public IMapProvider getMapProvider();

    public void setMapProvider(IMapProvider mapProvider);

    public IRuleSetProvider getRuleSetProvider();

    public void setRuleSetProvider(IRuleSetProvider ruleSetProvider);

    public IUserInterface getUserInterface();

    public void setUserInterface(IUserInterface userInterface);

    public IPlayer[] getPlayers();

    public void setPlayers(IPlayer[] players);

    public IBoard getBoard();

    public void setBoard(IBoard board);

    public IEventBus getEventBus();

    public IGameState getState();

    public IPlayer getCurrentPlayer();

    public int getCurrentTurnNumber();

    public int getCurrentDiceValue();

    public void reset();

    public void clear();
}
