package com.roundfeather.persistence.datastore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.roundfeather.persistence.datastore.DatastoreRepository;

/**
 * Mark an object as a Datastore entity
 *
 * <p>
 *     The value of the annotation is the name of the entity in Google Datastore
 * </p>
 *
 * <p>
 *     By default, annotating a class with this will automatically generate an implementation of {@link DatastoreRepository}.
 *     This can be disabled if you need to create your own complex implementation by setting the property {@code autoGenerateRepository}
 *     to {@code false}
 * </p>
 *
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreEntity {
    /**
     * Sets the Datastore entity name
     *
     * @return The name of the entity
     *
     * @since 1.0
     */
    String value();

    /**
     * Set if a repository should be generated for this class
     *
     * <p>
     *     default: {@code true}
     * </p>
     *
     *
     * @return If a repository should be generated for this class
     *
     * @since 1.0
     */
    boolean autoGenerateRepository() default true;
}
