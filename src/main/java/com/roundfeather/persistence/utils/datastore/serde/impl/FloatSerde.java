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
public class FloatSerde implements DataStoreObjectSerde<Float> {

    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o.getClass() == Float.class;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        log.debug(String.format("Serializing '%s' to a value of type 'DOUBLE'", o.getClass().getName()));
        return DoubleValue
                .newBuilder((float) o)
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type t, Value v) {
        return (t == Float.class || t == float.class) && v.getType() == ValueType.DOUBLE;
    }

    @Override
    public boolean canDeserialize(Class t, Value v) {
        return (t == Float.class || t == float.class) && v.getType() == ValueType.DOUBLE;
    }

    @Override
    public Float deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'float'", v.getType().name()));
        return ((Double) v.get()).floatValue();
    }

    @Override
    public Float deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'float'", v.getType().name()));
        return ((Double) v.get()).floatValue();
    }
}
