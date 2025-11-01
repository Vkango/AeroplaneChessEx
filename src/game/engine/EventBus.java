package game.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import game.api.IGameEvent;
import game.api.IEventBus;

public final class EventBus implements IEventBus {
    private static final EventBus INSTANCE = new EventBus();
    private final Map<String, List<IEventListener>> eventTypeListeners = new ConcurrentHashMap<>();

    private final Set<String> subscriptions = ConcurrentHashMap.newKeySet();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    @Override
    public void subscribe(String eventType, IEventListener listener) {
        String subscriberClass = getSubscriberClassName();
        String subscriptionKey = subscriberClass + ":" + eventType;
        if (subscriptions.contains(subscriptionKey)) {
            // System.out.println("[EventBus] 跳过重复订阅: " + eventType + " (订阅者: " +
            // subscriberClass + ")");
            return;
        }

        List<IEventListener> listeners = eventTypeListeners.computeIfAbsent(eventType,
                k -> new CopyOnWriteArrayList<>());
        listeners.add(listener);
        subscriptions.add(subscriptionKey);

        // System.out.println("[EventBus] 订阅事件: " + eventType + " (订阅者: " +
        // subscriberClass + ")");
    }

    private String getSubscriberClassName() {
        try {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (stack.length > 3) {
                String fullClassName = stack[3].getClassName();
                return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            }
        } catch (Exception e) {

        }
        return "Unknown";
    }

    @Override
    public void unsubscribe(String eventType, IEventListener listener) {
        String subscriberClass = getSubscriberClassName();
        String subscriptionKey = subscriberClass + ":" + eventType;

        List<IEventListener> listeners = eventTypeListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
            subscriptions.remove(subscriptionKey);
            if (listeners.isEmpty()) {
                eventTypeListeners.remove(eventType);
            }
        }
    }

    @Override
    public void publish(IGameEvent event) {
        String eventType = event.getType();
        List<IEventListener> listeners = eventTypeListeners.getOrDefault(eventType, Collections.emptyList());
        for (IEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                System.err.println("[EventBus] 处理事件 " + eventType + " 时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clear() {
        eventTypeListeners.clear();
        subscriptions.clear();
    }
}