package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;

import java.util.ArrayList;

import game.api.*;

public class Mine implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.RED;
    }

    @Override
    public String getType() {
        return "地雷";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {
        if (chessman != null && context != null) {
            ArrayList<String> choices = new ArrayList<String>();
            choices.add("被炸死");
            choices.add("被扎死");
            choices.add("被渣丝");
            context.getUserInterface().getUserChoice("选择你的死法", choices);
            String message = "[Mine] 玩家 " + context.getCurrentPlayer().getName() + " 的棋子 " + chessman.getChessmanId()
                    + " 踩到地雷，返回待起飞区！";
            java.util.Map<String, Object> effectData = new java.util.HashMap<>();
            effectData.put("chessman", chessman);
            effectData.put("message", message);

            IGameEvent event = new BlockEvent("BlockEffect", effectData, "方块效果触发");
            context.getEventBus().publish(event);
            chessman.setPosition(-1);
        }
    }

    @Override
    public String getDescription() {
        return "地雷方块，触发爆炸效果。";
    }

    @Override
    public String getTUISymbol() {
        return "地雷";
    }
}