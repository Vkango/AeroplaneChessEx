package game.api;

public interface IEventBus {

    void subscribe(String eventType, IEventListener listener);

    void unsubscribe(String eventType, IEventListener listener);

    void publish(IGameEvent event);

    void clear();

    interface IEventListener {
        void onEvent(IGameEvent event);
    }
}
