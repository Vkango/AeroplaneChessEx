package game.engine;

import game.api.IChessman;
import game.api.IPlayer;

public class Chessman implements IChessman {
    private int id;
    private IPlayer owner;
    private int position; // 棋子在棋盘上的位置

    public Chessman(int id, IPlayer owner) {
        this.id = id;
        this.owner = owner;
        this.position = -1; // 等待指派，初始位置为-1表示未起飞
    }

    @Override
    public int getChessmanId() {
        return id;
    }

    @Override
    public IPlayer getOwner() {
        return owner;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public void reset() {
        this.position = -1;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
        EventBus.getInstance().publish(new GameEvent("ChessmanMoved", this,
                "棋子 " + this.getChessmanId() + " 移动到了位置 " + position));
    }
}