package de.intelligence.drp.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.intelligence.drp.api.exception.ImplementationNotFoundException;

public final class Discord {

    private static final String IMPLEMENTATION = "de.intelligence.drp.core.dc.DiscordWrapper";

    public Discord() {
        // prevent instantiation
    }

    public static IDiscord create(String applicationId) {
        try {
            final Class<?> implClazz = Class.forName(Discord.IMPLEMENTATION);
            final Constructor<?> instConstructor = implClazz.getDeclaredConstructor(String.class);
            if(!instConstructor.trySetAccessible()) {
                throw new ImplementationNotFoundException("Failed to access implementation " + Discord.IMPLEMENTATION);
            }
            final Object instance = instConstructor.newInstance(applicationId);
            instConstructor.setAccessible(false);
            return (IDiscord) instance;
        } catch (ClassNotFoundException ex) {
            throw new ImplementationNotFoundException("Could not find implementation " + Discord.IMPLEMENTATION, ex);
        } catch (NoSuchMethodException ex) {
            throw new ImplementationNotFoundException("Failed to create implementation " + Discord.IMPLEMENTATION, ex);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new ImplementationNotFoundException("Failed to instantiate implementation " + Discord.IMPLEMENTATION, ex);
        }
    }

}
