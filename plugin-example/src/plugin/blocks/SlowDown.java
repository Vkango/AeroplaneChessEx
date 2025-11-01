package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;
import game.api.*;

public class SlowDown implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.GREEN;
    }

    @Override
    public String getType() {
        return "减速";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {
        if (chessman != null && context != null) {
            String message = "[SlowDown] 玩家 " + context.getCurrentPlayer().getName() + " 的棋子 "
                    + chessman.getChessmanId() + " 触发减速，返回并反向走相同步数！";
            java.util.Map<String, Object> effectData = new java.util.HashMap<>();
            effectData.put("chessman", chessman);
            effectData.put("message", message);
            IGameEvent event = new BlockEvent("BlockEffect", effectData, "方块效果触发");
            context.getEventBus().publish(event);
            IBoard board = context.getBoard();
            board.moveChessman(chessman, context.getCurrentDiceValue() * -2);
        }
    }

    @Override
    public String getDescription() {
        return "减速方块，让棋子后退并反向走动相同步数。";
    }

    @Override
    public String getTUISymbol() {
        return "减速";
    }
}
