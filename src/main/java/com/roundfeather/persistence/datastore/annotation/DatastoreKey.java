package com.roundfeather.persistence.datastore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field of a class annotated with {@link DatastoreEntity} as a Datastore key.
 *
 * <p>
 *     {@code keyType} determines if the key is a Long or String
 * </p>
 *
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreKey {

    /**
     * Sets the type of the key
     *
     * @return the type of key
     *
     * @since 1.0
     */
    KeyType keyType();
}
