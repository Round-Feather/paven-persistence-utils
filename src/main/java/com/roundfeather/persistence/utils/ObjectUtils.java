package com.roundfeather.persistence.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for interacting with objects
 *
 * @since 1.0
 */
public class ObjectUtils {

    /**
     * Gets the value of a field for an object using either a getter or direct access to public fields
     *
     * @param o Object to get field value from
     * @param f Field to get
     * @return The value of the field
     *
     * @since 1.0
     */
    public static Object getFieldValue(Object o, Field f) {
        if (f.canAccess(o)) {
            try {
                return f.get(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            String getMethodName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            Optional<Method> getMethod = Arrays.stream(o.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals(getMethodName))
                    .findFirst();

            if (!getMethod.isEmpty() && getMethod.get().canAccess(o)) {
                try {
                    return getMethod.get().invoke(o);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Sets the value of a field for an object using either a setter or direct access to public fields
     *
     * @param o Object to set field value from
     * @param f Field to set
     * @param v Value to set
     *
     * @since 1.0
     */
    public static void setFieldValue(Object o, Field f, Object v) {
        if (f.canAccess(o)) {
            try {
                f.set(o, v);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            String setMethodName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            Optional<Method> setMethod = Arrays.stream(o.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals(setMethodName))
                    .findFirst();

            if (!setMethod.isEmpty() && setMethod.get().canAccess(o)) {
                try {
                    setMethod.get().invoke(o, v);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
