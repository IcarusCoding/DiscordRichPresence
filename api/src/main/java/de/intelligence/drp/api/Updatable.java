package de.intelligence.drp.api;

public interface Updatable {

    void addObserver(Observer observer);

    void removeObserver(Observer observer);

    void update();

}
