package game.engine;

import java.time.Instant;
import game.api.IGameEvent;

public final class GameEvent implements IGameEvent {
    private final String type;
    private final long timestamp = Instant.now().toEpochMilli();
    private final String description;
    private final Object payload;

    public GameEvent(String type, Object payload) {
        this(type, payload, "");
    }

    public GameEvent(String type, Object payload, String description) {
        this.type = type;
        this.payload = payload;
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) payload;
    }
}