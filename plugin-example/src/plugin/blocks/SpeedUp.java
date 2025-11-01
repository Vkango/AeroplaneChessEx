package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;
import game.api.*;

public class SpeedUp implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.BLUE;
    }

    @Override
    public String getType() {
        return "加速";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {
        if (chessman != null && context != null) {
            String message = "[SpeedUp] 玩家 " + context.getCurrentPlayer().getName() + " 的棋子 " + chessman.getChessmanId()
                    + " 触发加速，2倍速前进！";
            java.util.Map<String, Object> effectData = new java.util.HashMap<>();
            effectData.put("chessman", chessman);
            effectData.put("message", message);
            IGameEvent event = new BlockEvent("BlockEffect", effectData, "方块效果触发");
            context.getEventBus().publish(event);
            IBoard board = context.getBoard();
            board.moveChessman(chessman, context.getCurrentDiceValue() * 2);
        }
    }

    @Override
    public String getDescription() {
        return "加速方块，让棋子额外前进。";
    }

    @Override
    public String getTUISymbol() {
        return "加速";
    }
}
