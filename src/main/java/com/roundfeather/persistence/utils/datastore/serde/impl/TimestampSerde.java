package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.TimestampValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.utils.datastore.DatastoreNamespace;
import com.roundfeather.persistence.utils.datastore.EntityManager;
import com.roundfeather.persistence.utils.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.reflect.Type;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.3
 */
@ApplicationScoped
public class TimestampSerde implements DataStoreObjectSerde<Long> {

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
        return TimestampValue
                .newBuilder(Timestamp.ofTimeMicroseconds((Long) o))
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type tp, Value v) {
        return (tp == Long.class || tp == long.class) && v.getType() == ValueType.TIMESTAMP;
    }

    @Override
    public boolean canDeserialize(Class tp, Value v) {
        return (tp == Long.class || tp == long.class) && v.getType() == ValueType.TIMESTAMP;
    }

    @Override
    public Long deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        return ((TimestampValue) v).get().toSqlTimestamp().getTime();
    }

    @Override
    public Long deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return ((TimestampValue) v).get().toSqlTimestamp().getTime();
    }
}
