package game.api;

import java.util.List;

import plugin.api.IMapProvider;

public interface IBoard {

    boolean moveChessman(IChessman chessman, int steps);

    List<IChessman> getChessmenAt(int position);

    boolean inDestination(IMapProvider mapProvider, IChessman chessman, int position);

    boolean inStart(IChessman chessman, int position);
}
