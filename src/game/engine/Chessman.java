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
        int oldPosition = this.position;
        this.position = position;
        if (oldPosition != position && oldPosition != -1) {
            java.util.Map<String, Object> moveData = new java.util.HashMap<>();
            moveData.put("chessman", this);
            moveData.put("from", oldPosition);
            moveData.put("to", position);
            EventBus.getInstance().publish(new GameEvent("ChessmanMoveEasing", moveData,
                    "棋子移动动画"));
        }
        EventBus.getInstance().publish(new GameEvent("ChessmanMoved", this,
                "棋子 " + this.getChessmanId() + " 移动到了位置 " + position));
    }
}