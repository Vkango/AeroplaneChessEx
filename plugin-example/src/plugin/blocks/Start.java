package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;
import game.api.*;

public class Start implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.PURPLE;
    }

    @Override
    public String getType() {
        return "开始";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {

    }

    @Override
    public String getDescription() {
        return "起点方块，游戏开始的位置。";
    }

    @Override
    public String getTUISymbol() {
        return "起点";
    }
}
