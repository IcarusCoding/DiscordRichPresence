package de.intelligence.drp.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class AnnotationUtils {

    private AnnotationUtils() {
        // prevent instantiation
    }

    public static List<Method> getMethodsAnnotatedBy(Class<? extends Annotation> annotationClazz, Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annotationClazz)).toList();
    }

}
