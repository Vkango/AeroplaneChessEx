package plugin.api;

import game.api.IChessman;
import game.api.IGameContext;

public interface IOverEndRule {

    int handleOverEnd(IChessman chessman, int deltaSteps, IGameContext context);
}
