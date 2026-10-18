// lang: Java  file: EventBus.java  target: Fabric 1.21.1
package dev.namtoo.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {

    private final Map<Class<?>, List<Object>> listeners = new ConcurrentHashMap<>();

    public void subscribe(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (!m.isAnnotationPresent(EventHandler.class)) continue;
            if (m.getParameterCount() != 1) continue;
            listeners.computeIfAbsent(m.getParameterTypes()[0], k -> new ArrayList<>()).add(obj);
        }
    }

    public void unsubscribe(Object obj) {
        listeners.values().forEach(l -> l.remove(obj));
    }

    public void post(Object event) {
        List<Object> targets = listeners.get(event.getClass());
        if (targets == null) return;
        for (Object t : new ArrayList<>(targets)) {
            try {
                for (Method m : t.getClass().getMethods()) {
                    if (!m.isAnnotationPresent(EventHandler.class)) continue;
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == event.getClass())
                        m.invoke(t, event);
                }
            } catch (Exception ignored) {}
        }
    }
}
