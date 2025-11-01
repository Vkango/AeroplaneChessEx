package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;
import game.api.*;

public class Destination implements IBlock {

    @Override
    public Colors getColor() {
        return Colors.YELLOW;
    }

    @Override
    public String getType() {
        return "终点";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {
        if (chessman == null || chessman.getOwner() == null || context == null) {
            return;
        }

        IPlayer player = chessman.getOwner();
        int endPosition = context.getMapProvider().getEndPosition(player.getPlayerId());

        System.out.println("[Destination] 玩家 " + player.getName() + " 的棋子 " +
                chessman.getChessmanId() + " 到达终点！当前位置=" + chessman.getPosition() +
                ", 终点位置=" + endPosition);

        boolean allChessmenFinished = true;
        int finishedCount = 0;
        for (IChessman c : player.getChessman()) {
            System.out.println("  - 棋子 " + c.getChessmanId() + " 位置=" + c.getPosition() +
                    " (终点=" + endPosition + ")");
            if (c.getPosition() >= endPosition) {
                finishedCount++;
            } else {
                allChessmenFinished = false;
            }
        }

        System.out.println("[Destination] 玩家 " + player.getName() + " 已有 " + finishedCount +
                "/" + player.getChessman().length + " 个棋子到达终点");

        String message = "[Destination] 玩家 " + player.getName() + " 已有 " + finishedCount +
                "/" + player.getChessman().length + " 个棋子到达终点";
        java.util.Map<String, Object> effectData = new java.util.HashMap<>();
        effectData.put("chessman", chessman);
        effectData.put("message", message);

        IGameEvent event = new BlockEvent("BlockEffect", effectData, "方块效果触发");
        context.getEventBus().publish(event);

        if (allChessmenFinished) {
            player.setWinner();
            context.getState().setWinner(player);
            context.getState().setGameOver(true);
            System.out.println("[Destination] 玩家 " + player.getName() + " 所有棋子都到达终点，获得胜利！");
        }
    }

    @Override
    public String getDescription() {
        return "终点方块";
    }

    @Override
    public String getTUISymbol() {
        return "终点";
    }
}
