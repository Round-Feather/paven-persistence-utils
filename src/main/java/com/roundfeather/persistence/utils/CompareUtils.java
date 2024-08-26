package com.roundfeather.persistence.utils;

import com.roundfeather.persistence.utils.datastore.annotation.DatastoreKey;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.roundfeather.persistence.utils.ObjectUtils.getFieldValue;
import static com.roundfeather.persistence.utils.ObjectUtils.setFieldValue;

/**
 * Utility class for comparing objects
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class CompareUtils {

    private static final List<Class> PRIMITIVE_CLASSES = List.of(
            Long.class,
            long.class,
            Integer.class,
            int.class,
            Boolean.class,
            boolean.class,
            Float.class,
            float.class,
            Double.class,
            double.class,
            String.class
    );

    /**
     * Handles comparing nullable values of two objects to determine if they're the same
     *
     * @param a Object to compare
     * @param b Other Object to compare
     * @return If objects a and b are the same
     *
     * @since 1.0
     */
    private static <T> boolean handleNulls(T a, T b) {
        if (a == null && b == null) {
            return true;
        } else if (a != null && b == null) {
            return false;
        } else if (a == null && b != null) {
            return false;
        }

        return false;
    }

    /**
     * Compares two lists of Objects to determine if they're the same
     *
     * @param a List of Object to compare
     * @param b Other List of Object to compare
     * @param ignoreKeyFields If fields annotated with {@link DatastoreKey} should be ignored
     * @return If objects a and b are the same
     *
     * @since 1.0
     */
    private static boolean handleLists(List a, List b, boolean ignoreKeyFields) {
        if (a.size() != b.size()) {
            return false;
        }

        if (a.isEmpty()) {
            return true;
        }

        int i = 0;
        for (Object o: a) {
            boolean same = isSame(o, a.get(i), ignoreKeyFields);
            if (!same) {
                return false;
            }
            i = i + 1;
        }

        return false;
    }

    /**
     * Compares two objects to determine if they're the same
     *
     * @param a Object to compare
     * @param b Other Object to compare
     * @return If objects a and b are the same
     *
     * @since 1.0
     */
    public static <T> boolean isSame(T a, T b) {
        return isSame(a, b, false);
    }

    /**
     * Compares two objects to determine if they're the same
     *
     * @param a Object to compare
     * @param b Other Object to compare
     * @param ignoreKeyFields If fields annotated with {@link DatastoreKey} should be ignored
     * @return If objects a and b are the same
     *
     * @since 1.0
     */
    public static <T> boolean isSame(T a, T b, boolean ignoreKeyFields) {
        if (a == null || b == null) {
            return handleNulls(a, b);
        }

        if (PRIMITIVE_CLASSES.contains(a.getClass())) {
            return a.equals(b);
        }

        if (a instanceof List) {
            return handleLists((List) a, (List) b, ignoreKeyFields);
        }


        List<Field> fields = Arrays.stream(a.getClass().getDeclaredFields()).toList();
        for (Field f: fields) {
            if (!ignoreKeyFields || f.getAnnotation(DatastoreKey.class) == null) {
                boolean same = isSame(getFieldValue(a, f), getFieldValue(b, f), ignoreKeyFields);
                if (!same) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Updates the fields in one object with the non-null fields of another
     *
     * @param source The Object to find non-null fields in
     * @param target The object to update
     * @return The updated target object
     *
     * @since 1.0
     */
    public static <T> T mergeNonNullFields(T source, T target) {
        return mergeNonNullFields(source, target, false);
    }

    /**
     * Updates the fields in one object with the non-null fields of another
     *
     * @param source The Object to find non-null fields in
     * @param target The object to update
     * @param ignoreKeyFields If fields annotated with {@link DatastoreKey} should be ignored
     * @return The updated target object
     *
     * @since 1.0
     */
    public static <T> T mergeNonNullFields(T source, T target, boolean ignoreKeyFields) {
        if (source == null) {
            return target;
        }

        if (PRIMITIVE_CLASSES.contains(source.getClass())) {
            return source;
        }

        if (source instanceof List) {
            return source;
        }

        Arrays.stream(source.getClass().getDeclaredFields()).toList()
                .forEach(
                        f -> {
                            if (!ignoreKeyFields || f.getAnnotation(DatastoreKey.class) == null) {
                                setFieldValue(target, f, mergeNonNullFields(getFieldValue(source, f), getFieldValue(target, f), ignoreKeyFields));
                            }
                        }
                );

        return target;
    }
}
