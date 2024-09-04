package com.roundfeather.persistence.utils.datastore.annotation;

/**
 * This enum is used to identify the type of property to use to determine subtypes
 *
 * @since 1.1
 */
public enum InclusionType {
    /**
     * Property to use for determining subtype is external to the object
     *
     * @since 1.1
     */
    EXTERNAL_PROPERTY,

    /**
     * Property to use for determining subtype is internal to the object
     *
     * @since 1.1
     */
    INTERNAL_PROPERTY
}
