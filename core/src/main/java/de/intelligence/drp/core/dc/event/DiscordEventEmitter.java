package de.intelligence.drp.core.dc.event;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import de.intelligence.drp.api.Subscribable;
import de.intelligence.drp.api.annotation.AutoSubscribe;
import de.intelligence.drp.api.annotation.DiscordEventHandler;
import de.intelligence.drp.api.annotation.EventMetadata;
import de.intelligence.drp.api.event.DiscordEvent;
import de.intelligence.drp.api.event.ErrorEvent;
import de.intelligence.drp.core.event.IEventEmitter;
import de.intelligence.drp.core.util.AnnotationUtils;
import de.intelligence.drp.core.util.Pair;

public final class DiscordEventEmitter implements IEventEmitter<DiscordEvent> {

    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventListener>> handlerMethods;
    private final ThreadLocal<Queue<Pair<Object, List<EventListener>>>> threadLocalQueue;
    private final Subscribable subscribable;

    public DiscordEventEmitter(Subscribable subscribable) {
        this.subscribable = Objects.requireNonNull(subscribable);
        this.handlerMethods = new ConcurrentHashMap<>();
        this.threadLocalQueue = ThreadLocal.withInitial(ArrayDeque::new);
    }

    @Override
    public void emit(DiscordEvent event) {
        Objects.requireNonNull(event);
        if (this.emit0(event)) {
            return;
        }
        if (event.getClass().equals(ErrorEvent.class)) {
            System.out.println("NO ERROR HANDLER FOUND RIP RIP RIP!!!");
        }
    }

    @Override
    public void register(Object obj) {
        Objects.requireNonNull(obj);
        boolean autoSubscribe = obj.getClass().isAnnotationPresent(AutoSubscribe.class);
        for (final Method m : AnnotationUtils.getMethodsAnnotatedBy(DiscordEventHandler.class, obj.getClass())) {
            if (m.getParameterCount() != 1) {
                continue;
            }
            final Class<?> eventParamType = m.getParameterTypes()[0];
            if (!DiscordEvent.class.isAssignableFrom(eventParamType)) {
                continue;
            }
            if (!this.handlerMethods.containsKey(eventParamType)) {
                this.handlerMethods.put(eventParamType, new CopyOnWriteArrayList<>());
            }
            this.handlerMethods.get(eventParamType)
                    .add(new EventListener(obj, m, m.getAnnotation(DiscordEventHandler.class).priority()));
            if (autoSubscribe && eventParamType.isAnnotationPresent(EventMetadata.class)) {
                final EventMetadata metadata = eventParamType.getAnnotation(EventMetadata.class);
                if (metadata.needsSubscription()) {
                    this.subscribable.subscribe(metadata.eventType());
                }
            }
        }
    }

    @Override
    public void unregister(Object obj) {
        Objects.requireNonNull(obj);
        boolean autoSubscribe = obj.getClass().isAnnotationPresent(AutoSubscribe.class);
        for (final Method m : AnnotationUtils.getMethodsAnnotatedBy(DiscordEventHandler.class, obj.getClass())) {
            if (m.getParameterCount() != 1) {
                continue;
            }
            final Class<?> eventParamType = m.getParameterTypes()[0];
            if (!DiscordEvent.class.isAssignableFrom(eventParamType)) {
                continue;
            }
            if (!this.handlerMethods.containsKey(eventParamType)) {
                continue;
            }
            this.handlerMethods.get(eventParamType).removeIf(e -> e.method.equals(m));
            if (autoSubscribe && eventParamType.isAnnotationPresent(EventMetadata.class)) {
                final EventMetadata metadata = eventParamType.getAnnotation(EventMetadata.class);
                if (metadata.needsSubscription()) {
                    this.subscribable.unsubscribe(metadata.eventType());
                }
            }
        }
    }

    private boolean emit0(Object obj) {
        if (!this.handlerMethods.containsKey(obj.getClass())) {
            return false;
        }
        final Queue<Pair<Object, List<EventListener>>> queue = this.threadLocalQueue.get();
        queue.offer(Pair.of(obj, this.handlerMethods.get(obj.getClass()).stream()
                .sorted(Comparator.comparing(EventListener::priority).reversed()).collect(Collectors.toList())));
        final AtomicReference<Pair<Object, List<EventListener>>> currentPair = new AtomicReference<>(queue.poll());
        while (currentPair.get() != null) {
            currentPair.get().getRight().forEach(listener -> {
                boolean canAccess = listener.method.canAccess(listener.subObj);
                if (!canAccess && !listener.method.trySetAccessible()) {
                    return;
                }
                try {
                    listener.method.invoke(listener.subObj, currentPair.get().getLeft());
                } catch (ReflectiveOperationException ignored) {
                }
                if (!canAccess) {
                    listener.method.setAccessible(false);
                }
            });
            currentPair.set(queue.poll());
        }
        return true;
    }

    private record EventListener(Object subObj, Method method, int priority) {
    }

}
