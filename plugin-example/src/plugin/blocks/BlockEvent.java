package plugin.blocks;

import game.api.IGameEvent;
import java.util.Map;

class BlockEvent implements IGameEvent {
    private final String type;
    private final Map<String, Object> data;
    private final String description;
    private final long timestamp;

    public BlockEvent(String type, Map<String, Object> data, String description) {
        this.type = type;
        this.data = data;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
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
        return (T) data;
    }
}
