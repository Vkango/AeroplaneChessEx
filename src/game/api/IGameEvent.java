package game.api;

public interface IGameEvent {

    String getType();

    long getTimestamp();

    String getDescription();

    <T> T getData();
}
