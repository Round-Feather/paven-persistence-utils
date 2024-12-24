package com.roundfeather.persistence.utils.datastore.serde;

import com.google.cloud.datastore.Value;
import com.roundfeather.persistence.utils.datastore.EntityManager;

/**
 * Simplified serde interface to overwrite default serdes
 *
 * @param <T> Type of object to serialize and deserialize.
 *
 * @since 1.3
 */
@SuppressWarnings({"squid:S3740"})
public interface CustomSerde<T> {

    /**
     * Serialize an {@link Object} to a Datastore {@link Value}
     *
     * @param em EntityManager for handling complex objects
     * @param o Object to serialize
     * @param excludeFromIndex If the {@link Value} should be excluded from the index
     * @return serialized Datastore {@link Value}
     *
     * @since 1.3
     */
    Value serialize(EntityManager em, T o, boolean excludeFromIndex);

    /**
     * Deserialize a Datastore {@link Value} to an object of type {@code tp}
     *
     * @param em EntityManager for handling complex objects
     * @param v Datastore Value to deserialize
     * @return deserialized object
     *
     * @since 1.3
     */
    T deserialize(EntityManager em, Value v);
}
