package com.roundfeather.persistence.datastore.serde.impl;

import com.google.cloud.datastore.LongValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.datastore.DatastoreNamespace;
import com.roundfeather.persistence.datastore.EntityManager;
import com.roundfeather.persistence.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.reflect.Type;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@ApplicationScoped
public class LongSerde implements DataStoreObjectSerde<Long> {

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o.getClass() == Long.class;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        return LongValue
                .newBuilder((long) o)
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type t, Value v) {
        return (t == Long.class || t == long.class) && v.getType() == ValueType.LONG;
    }

    @Override
    public boolean canDeserialize(Class t, Value v) {
        return (t == Long.class || t == long.class) && v.getType() == ValueType.LONG;
    }

    @Override
    public Long deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        return (long) v.get();
    }

    @Override
    public Long deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return (long) v.get();
    }
}
