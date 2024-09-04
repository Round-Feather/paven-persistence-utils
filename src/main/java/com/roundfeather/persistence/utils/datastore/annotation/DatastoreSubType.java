package com.roundfeather.persistence.utils.datastore.annotation;

import java.lang.annotation.*;
import com.google.cloud.datastore.Entity;

/**
 * Define the Class to use when deserializing an {@link Entity} based on some field property.
 *
 * @since 1.1
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreSubType {
    /**
     * Sets the Class to use
     *
     * @return the class to deserialize as
     *
     * @since 1.1
     */
    Class type();

    /**
     * Sets the value the property needs to match to use this type
     *
     * @return The value the property needs to match
     *
     * @since 1.1
     */
    String name() default "";

    /**
     * Sets a list of possible values the property needs to match to use this type
     *
     * @return The list of values the property can match
     *
     * @since 1.1
     */
    String[] names() default {};
}
