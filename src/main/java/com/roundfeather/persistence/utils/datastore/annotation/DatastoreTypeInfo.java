package com.roundfeather.persistence.utils.datastore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide metadata around what property is used to determine subtypes of a field
 *
 * @since 1.1
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DatastoreTypeInfo {
    /**
     * The type of property to use when determining subtypes
     *
     * @return the type of property to include
     *
     * @since 1.1
     */
    InclusionType include() default InclusionType.INTERNAL_PROPERTY;

    /**
     * Sets the property name to use to determine subtypes
     *
     * @return the property to use to determine subtypes
     *
     * @since 1.1
     */
    String property();

    /**
     * Sets the default subtype implementation
     *
     * @return the default subtype implementation
     *
     * @since 1.2
     */
    Class<?> defaultImpl() default DatastoreTypeInfo.class;
}
