package plugin.blocks;

import plugin.api.IBlock;
import ui.api.Colors;
import game.api.*;

public class Empty implements IBlock {
    @Override
    public Colors getColor() {
        return Colors.WHITE;
    }

    @Override
    public String getType() {
        return "Empty";
    }

    @Override
    public void onLand(IChessman chessman, IGameContext context) {
        throw new IllegalStateException("飞机不可能走到空白方块上");
    }

    @Override
    public String getDescription() {
        return "空白方块，永不可及。";
    }

    @Override
    public String getTUISymbol() {
        return "  ";
    }
}