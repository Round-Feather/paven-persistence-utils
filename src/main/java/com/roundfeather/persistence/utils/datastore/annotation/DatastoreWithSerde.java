package com.roundfeather.persistence.utils.datastore.annotation;

import com.roundfeather.persistence.utils.datastore.serde.CustomSerde;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field of a class annotated with {@link DatastoreEntity} with a {@link CustomSerde} to use.
 *
 * <p>
 *     The value of the annotation is the custom serde class to use
 * </p>
 *
 * @since 1.3
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreWithSerde {
    /**
     * Sets the custom serde
     *
     * @return The class of the custom serde
     *
     * @since 1.3
     */
    Class<? extends CustomSerde> value();
}
