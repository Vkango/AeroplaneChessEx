package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;

import java.util.ArrayList;
import java.util.Random;

import game.api.*;

public class Lucky implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.ORANGE;
    }

    private static final Random random = new Random();

    @Override
    public String getType() {
        return "幸运";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {
        if (chessman != null && context != null) {
            ArrayList<String> choices = new ArrayList<String>();
            java.util.Map<String, Object> effectData = new java.util.HashMap<>();
            choices.add("幸运1");
            choices.add("幸运2");
            choices.add("幸运3");
            context.getUserInterface().getUserChoice("选择一个运气", choices);
            int randomAction = random.nextInt(4);
            switch (randomAction) {
                case 0: // 前进
                    int endPosition = context.getMapProvider().getEndPosition(chessman.getOwner().getPlayerId());
                    int currentPos = chessman.getPosition();
                    int newPos = random.nextInt(endPosition - currentPos) + currentPos + 1; // 保证一定正向动，teleport是双向动

                    int attempts = 0;
                    while ((newPos == currentPos || newPos == 0) && attempts < 50) {
                        newPos = random.nextInt(endPosition - currentPos) + currentPos + 1;
                        attempts++;
                    }
                    String message = "[Lucky] 玩家 " + context.getCurrentPlayer().getName() + " 的棋子 "
                            + chessman.getChessmanId()
                            + " 触发幸运，前进" + (newPos - currentPos) + "步！";

                    effectData.put("chessman", chessman);
                    effectData.put("message", message);
                    context.getEventBus().publish(new BlockEvent("BlockEffect", effectData, "方块效果触发"));

                    IBoard board = context.getBoard();
                    board.moveChessman(chessman, newPos - currentPos);
                    break;
                case 1:
                    effectData.put("chessman", chessman);
                    effectData.put("message", "[Lucky] 玩家 " + context.getCurrentPlayer().getName() + " 的棋子 "
                            + chessman.getChessmanId()
                            + " 触发幸运，额外一轮送上！");
                    context.getEventBus().publish(new BlockEvent("GrantNewTurn", effectData, "该玩家再来一轮"));
                    context.getEventBus().publish(new BlockEvent("BlockEffect", effectData, "方块效果触发"));
                    // 后退2步
                    break;
                case 2:
                    // 保持不动
                    choices.clear();
                    choices.add("选我");
                    choices.add("漩涡");
                    choices.add("泫沃");
                    context.getUserInterface().getUserChoice("你有可能直接把棋子传送到终点！请选择机会", choices);
                    if (random.nextInt(3) == 0) {
                        // if (true) {
                        int endPos = context.getMapProvider().getEndPosition(chessman.getOwner().getPlayerId());
                        String msg = "[Lucky] 玩家 " + context.getCurrentPlayer().getName() + " 的棋子 "
                                + chessman.getChessmanId()
                                + " 触发幸运，直接传送到终点！";
                        effectData.put("chessman", chessman);
                        effectData.put("message", msg);
                        context.getEventBus().publish(new BlockEvent("BlockEffect", effectData, "方块效果触发"));
                        chessman.setPosition(endPos);
                    } else {

                        String msg = "[Lucky] 很遗憾，您未中奖。";
                        effectData.put("chessman", chessman);
                        effectData.put("message", msg);
                        context.getEventBus().publish(new BlockEvent("BlockEffect", effectData, "方块效果触发"));
                    }
                    break;
                case 3: {
                    String msg = "[Lucky] 很遗憾，您未中奖。";
                    effectData.put("chessman", chessman);
                    effectData.put("message", msg);
                    context.getEventBus().publish(new BlockEvent("BlockEffect", effectData, "方块效果触发"));
                    break;
                }
            }

        }
    }

    @Override
    public String getDescription() {
        return "幸运方块";
    }

    @Override
    public String getTUISymbol() {
        return "幸运";
    }
}