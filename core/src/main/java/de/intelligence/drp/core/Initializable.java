package de.intelligence.drp.core;

import de.intelligence.drp.core.exception.AlreadyInitializedException;
import de.intelligence.drp.core.exception.InitializationFailedException;

public interface Initializable {

    boolean isInitialized();

    void initialize() throws InitializationFailedException, AlreadyInitializedException;

}
