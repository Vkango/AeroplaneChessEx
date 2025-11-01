package game.api;

import java.util.List;

public interface IGameEngine {

    void initialize(List<IPlayer> players);

    void start();

    void pause();

    void resume();

    void stop();

    void playTurn();
}
