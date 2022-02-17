package de.intelligence.drp.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.intelligence.drp.api.event.EventType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventMetadata {

    boolean needsSubscription() default false;

    EventType eventType() default EventType.NONE;

}
