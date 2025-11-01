package game.api;

public interface IChessman {

    int getChessmanId();

    IPlayer getOwner();

    int getPosition();

    void setPosition(int position);

}
