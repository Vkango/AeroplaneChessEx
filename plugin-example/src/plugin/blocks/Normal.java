package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;
import game.api.*;

public class Normal implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.GRAY;
    }

    @Override
    public String getType() {
        return "普通";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {

    }

    @Override
    public String getDescription() {
        return "普通方块，无特殊效果。";
    }

    @Override
    public String getTUISymbol() {
        return "□□";
    }

}
