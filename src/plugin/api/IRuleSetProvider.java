package plugin.api;

import game.api.*;

public interface IRuleSetProvider extends IPlugin {

    String getRuleSetName();

    ITakeOffCondition getTakeOffCondition();

    IMoveStrategy getMoveStrategy();

    boolean isGameOver(IPlayer[] players);

    IPlayer getWinner(IPlayer[] players);

    boolean shouldGrantExtraTurn(int diceValue);

    IOverEndRule getOverEndRule();

    int getMaxBlockEffectDepth();
}
