package de.intelligence.drp.core.event;

public interface IEventEmitter<T> {

    void emit(T obj);

    void register(Object obj);

    void unregister(Object obj);

}
