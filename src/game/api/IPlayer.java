package game.api;

public interface IPlayer {

    int getPlayerId();

    String getName();

    IChessman[] getChessman();

    boolean isWinner();

    boolean setWinner();

    boolean isActive();

    void setActive(boolean active);
}
