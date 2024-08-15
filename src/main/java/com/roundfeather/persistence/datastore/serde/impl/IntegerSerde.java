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
public class IntegerSerde implements DataStoreObjectSerde<Integer> {

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o.getClass() == Integer.class;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        return LongValue
                .newBuilder((int) o)
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type t, Value v) {
        return (t == Integer.class || t == int.class) && v.getType() == ValueType.LONG;
    }

    @Override
    public boolean canDeserialize(Class t, Value v) {
        return (t == Integer.class || t == int.class) && v.getType() == ValueType.LONG;
    }

    @Override
    public Integer deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        return ((Long) v.get()).intValue();
    }

    @Override
    public Integer deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return ((Long) v.get()).intValue();
    }
}
