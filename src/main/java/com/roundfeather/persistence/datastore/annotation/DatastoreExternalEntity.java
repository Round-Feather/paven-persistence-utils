package com.roundfeather.persistence.datastore.annotation;

import com.roundfeather.persistence.datastore.ChildAncestorsFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field of a class annotated with {@link DatastoreEntity} as an external entity and will be automatically queried
 * and embedded.
 *
 * <p>
 *     {@code ancestorFactory} specified the implementation of {@link ChildAncestorsFactory} to use to derive the ancestors
 *     for the external entities to embed
 * </p>
 *
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreExternalEntity {
    /**
     * Sets the {@link ChildAncestorsFactory} for this external entity field
     *
     * @return The {@link ChildAncestorsFactory} to for this external entity
     *
     * @since 1.0
     */
    Class<? extends ChildAncestorsFactory> ancestorFactory();
}
