package plugin.api;

import game.api.*;

public interface IMoveStrategy {

    boolean canMove(IChessman chessman, int steps, IGameContext context);

    boolean canCapture(IChessman attacker, IChessman defender);
}
