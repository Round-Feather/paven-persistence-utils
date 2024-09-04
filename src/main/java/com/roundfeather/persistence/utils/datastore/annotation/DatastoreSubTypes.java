package com.roundfeather.persistence.utils.datastore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wrapper for defining the Classes to use to deserialize the field as based on a property
 *
 * @since 1.1
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreSubTypes {
    /**
     * Sets the list of possible subtypes
     *
     * @return the list of subtypes
     *
     * @since 1.1
     */
    DatastoreSubType[] value();
}
