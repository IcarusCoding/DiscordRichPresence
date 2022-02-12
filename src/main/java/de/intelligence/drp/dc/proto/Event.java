package de.intelligence.drp.dc.proto;

public enum Event {
    //TODO differentiate between internal events and public api events to ensure correct subscriptions
    NONE,
    ERROR,
    INVALID,
    READY,

    // api
    ACTIVITY_JOIN

}
