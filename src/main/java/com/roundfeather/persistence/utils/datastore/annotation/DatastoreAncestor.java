package com.roundfeather.persistence.utils.datastore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field of a class annotated with {@link DatastoreEntity} as a Datastore ancestor.
 *
 * <p>
 *     {@code keyType} determines if the key is a Long or String
 * </p>
 * <p>
 *     {@code kind} defines the name of the kind associated with this ancestor
 * </p>
 * <p>
 *     {@code order} determines which order this ancestor should be applied in. Order should start at {@code 1}
 * </p>
 *
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreAncestor {

    /**
     * Sets the type of the key
     *
     * @return the type of key
     *
     * @since 1.0
     */
    KeyType keyType();

    /**
     * Sets the kind of the ancestor
     *
     * @return the kind of the ancestor
     *
     * @since 1.0
     */
    String kind();

    /**
     * Sets the order for this ancestor
     *
     * <p>
     *     default: {@code 1}
     * </p>
     *
     * @return the order of the ancestor
     *
     * @since 1.0
     */
    int order() default 1;
}
