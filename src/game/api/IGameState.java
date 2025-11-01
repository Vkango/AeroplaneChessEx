package game.api;

public interface IGameState {

    boolean isRunning();

    void setRunning(boolean running);

    boolean isPaused();

    void setPaused(boolean paused);

    boolean isGameOver();

    void setGameOver(boolean gameOver);

    IPlayer getCurrentPlayer();

    void setCurrentPlayer(IPlayer player);

    int getTurnNumber();

    void setTurnNumber(int turnNumber);

    int getCurrentDiceValue();

    void setCurrentDiceValue(int diceValue);

    IPlayer getWinner();

    void setWinner(IPlayer winner);

    int getCurrentPlayerIndex();

    void setCurrentPlayerIndex(int index);

    boolean hasExtraTurn();

    void setExtraTurn(boolean extraTurn);

    void reset();
}
