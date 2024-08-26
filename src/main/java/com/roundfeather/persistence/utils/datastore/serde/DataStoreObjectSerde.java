package com.roundfeather.persistence.utils.datastore.serde;

import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Value;
import com.roundfeather.persistence.utils.datastore.DatastoreNamespace;
import com.roundfeather.persistence.utils.datastore.EntityManager;

import java.lang.reflect.Type;

/**
 * Serde for serializing and deserializing Datastore {@link Value} to and from Java Objects
 *
 * @param <T> Type of Object to being serialized/deserialized
 *
 * @since 1.0.0
 */
public interface DataStoreObjectSerde<T>{

    /**
     * Get the type of Object this serde supports
     *
     * @return The type this serde can support
     */
    Class<T> getType();

    /**
     * Predicate to determine if the Object is serializable by this serde
     *
     * @param o Object to check
     * @return If Object can be serialized
     */
    boolean canSerialize(Object o);

    /**
     * Serialize an {@link Object} to a Datastore {@link Value}
     *
     * @param em EntityManager for handling complex objects
     * @param o Object to serialize
     * @param excludeFromIndex If the {@link Value} should be excluded from the index
     * @return serialized Datastore {@link Value}
     */
    Value serialize(EntityManager em, Object o, boolean excludeFromIndex);

    /**
     * Serialize an {@link Object} to a Datastore {@link Value}
     *
     * @param em EntityManager for handling complex objects
     * @param o Object to serialize
     * @param excludeFromIndex If the {@link Value} should be excluded from the index
     * @param key key for the entity
     * @return serialized Datastore {@link Value}
     */
    default Value serialize(EntityManager em, Object o, boolean excludeFromIndex, IncompleteKey key) {
        return null;
    }

    /**
     * Predicate to determine if the {@link Value} is deserializable by this serde
     *
     * @param tp Parameterized type to deserialize to
     * @param v Value to check
     * @return If Value can be deserialized
     */
    boolean canDeserialize(Type tp, Value v);

    /**
     * Predicate to determine if the {@link Value} is deserializable by this serde
     *
     * @param tp Class to deserialize to
     * @param v Value to check
     * @return If Value can be deserialized
     */
    boolean canDeserialize(Class tp, Value v);

    /**
     * Deserialize a Datastore {@link Value} to an object of parameterized type {@code tp}
     *
     * @param dsNamespace Namespace of parent Entity
     * @param em EntityManager for handling complex objects
     * @param v Datastore Value to deserialize
     * @param tp Parameterized type of object to create
     * @return deserialized object
     */
    T deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp);

    /**
     * Deserialize a Datastore {@link Value} to an object of type {@code tp}
     *
     * @param dsNamespace Namespace of parent Entity
     * @param em EntityManager for handling complex objects
     * @param v Datastore Value to deserialize
     * @param tp Class of object to create
     * @return deserialized object
     */
    T deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp);
}
