package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;
import game.api.*;

import java.util.ArrayList;
import java.util.Random;

public class Teleport implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.CYAN;
    }

    private static final Random random = new Random();

    @Override
    public String getType() {
        return "传送";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {
        if (chessman != null && context != null) {
            ArrayList<String> choices = new ArrayList<String>();
            choices.add("传送到申必位置1");
            choices.add("传送到申必位置2");
            choices.add("传送到申必位置3");
            context.getUserInterface().getUserChoice("选择一个位置进行传送", choices);
            int endPosition = context.getMapProvider().getEndPosition(chessman.getOwner().getPlayerId());
            int currentPos = chessman.getPosition();
            int newPos = random.nextInt(endPosition);
            int attempts = 0;
            while ((newPos == currentPos || newPos == 0) && attempts < 50) {
                newPos = random.nextInt(endPosition);
                attempts++;
            }
            String message = "[Teleport] 棋子 " + chessman.getChessmanId() + " 触发传送，传送到位置 " + newPos + "！";
            java.util.Map<String, Object> effectData = new java.util.HashMap<>();
            effectData.put("chessman", chessman);
            effectData.put("message", message);
            IGameEvent event = new BlockEvent("BlockEffect", effectData, "方块效果触发");
            context.getEventBus().publish(event);

            chessman.setPosition(newPos);
        }
    }

    @Override
    public String getDescription() {
        return "传送方块，随机传送到地图上的其他位置。";
    }

    @Override
    public String getTUISymbol() {
        return "传送";
    }
}
