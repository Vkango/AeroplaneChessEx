package game.engine;

import game.api.IPlayer;
import game.api.IChessman;

public class Player implements IPlayer {
    private String name;
    private int playerId;
    private boolean isWinner = false;
    private IChessman[] chessmen;
    private boolean active;

    public Player(String name, int playerId, int chessmanCount) {
        this.name = name;
        this.playerId = playerId;
        this.active = true;
        this.chessmen = new IChessman[chessmanCount];
        for (int i = 0; i < chessmen.length; i++) {
            this.chessmen[i] = new Chessman(i, this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isWinner() {
        return this.isWinner;
    }

    @Override
    public boolean setWinner() {
        this.isWinner = true;
        return this.isWinner;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int getPlayerId() {
        return playerId;
    }

    @Override
    public IChessman[] getChessman() {
        return chessmen;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void reset() {
        this.isWinner = false;
        this.active = true;
        // 重置所有棋子
        for (IChessman chessman : chessmen) {
            if (chessman instanceof Chessman) {
                ((Chessman) chessman).reset();
            }
        }
    }
}