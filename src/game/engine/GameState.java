package game.engine;

import game.api.IGameState;
import game.api.IPlayer;

public class GameState implements IGameState {
    private boolean running = false;
    private boolean paused = false;
    private boolean gameOver = false;
    private IPlayer currentPlayer = null;
    private int turnNumber = 1;
    private int currentDiceValue = 0;
    private IPlayer winner = null;
    private int currentPlayerIndex = -1;
    private boolean extraTurn = false;

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    @Override
    public IPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public void setCurrentPlayer(IPlayer currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    @Override
    public int getTurnNumber() {
        return turnNumber;
    }

    @Override
    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    @Override
    public int getCurrentDiceValue() {
        return currentDiceValue;
    }

    @Override
    public void setCurrentDiceValue(int currentDiceValue) {
        this.currentDiceValue = currentDiceValue;
    }

    @Override
    public IPlayer getWinner() {
        return winner;
    }

    @Override
    public void setWinner(IPlayer winner) {
        this.winner = winner;
    }

    @Override
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    @Override
    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    @Override
    public boolean hasExtraTurn() {
        return extraTurn;
    }

    @Override
    public void setExtraTurn(boolean extraTurn) {
        this.extraTurn = extraTurn;
    }

    @Override
    public void reset() {
        this.running = false;
        this.paused = false;
        this.gameOver = false;
        this.currentPlayer = null;
        this.turnNumber = 1;
        this.currentDiceValue = 0;
        this.winner = null;
        this.currentPlayerIndex = -1;
        this.extraTurn = false;
    }
}
