package plugin.api;

import ui.api.Colors;


public interface IMapProvider extends IPlugin {

    String getMapName();

    Node[][] getMap();

    int getMapSize();

    int getDifficulty();

    int getRecommendedPlayers();

    int[] positionToXY(int position);

    int getStartPosition(int playerIndex);

    int getEndPosition(int playerIndex);

    int getMaxChessmanPerPlayer();

    int getMaxPlayers();

    int getMinPlayers();

    Colors[] getPlayerColors();

    String[] getPlayerColorsName();
}
