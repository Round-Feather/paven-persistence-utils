package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.datastore.DoubleValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.utils.datastore.DatastoreNamespace;
import com.roundfeather.persistence.utils.datastore.EntityManager;
import com.roundfeather.persistence.utils.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@ApplicationScoped
@Slf4j
public class DoubleSerde implements DataStoreObjectSerde<Double> {

    @Override
    public Class<Double> getType() {
        return Double.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o.getClass() == Double.class;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        log.debug(String.format("Serializing '%s' to a value of type 'DOUBLE'", o.getClass().getName()));
        return DoubleValue
                .newBuilder((double) o)
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type tp, Value v) {
        return (tp == Double.class || tp == double.class) && v.getType() == ValueType.DOUBLE;
    }

    @Override
    public boolean canDeserialize(Class tp, Value v) {
        return (tp == Double.class || tp == double.class) && v.getType() == ValueType.DOUBLE;
    }

    @Override
    public Double deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'double'", v.getType().name()));
        return (double) v.get();
    }

    @Override
    public Double deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'double'", v.getType().name()));
        return (double) v.get();
    }
}
