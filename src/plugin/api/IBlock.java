package plugin.api;

import game.api.*;
import ui.api.Colors;

public interface IBlock {

    String getType();

    void onLand(IChessman chessman, IGameContext context);

    String getDescription();

    Colors getColor();

    String getTUISymbol();
}
