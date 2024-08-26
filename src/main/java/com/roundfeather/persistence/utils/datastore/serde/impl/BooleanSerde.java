package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.datastore.BooleanValue;
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
public class BooleanSerde implements DataStoreObjectSerde<Boolean> {

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o.getClass() == Boolean.class;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        log.debug(String.format("Serializing '%s' to a value of type 'BOOLEAN'", o.getClass().getName()));

        return BooleanValue
                .newBuilder((boolean) o)
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type tp, Value v) {
        return (tp == Boolean.class || tp == boolean.class) && v.getType() == ValueType.BOOLEAN;
    }

    @Override
    public boolean canDeserialize(Class tp, Value v) {
        return (tp == Boolean.class || tp == boolean.class) && v.getType() == ValueType.BOOLEAN;
    }

    @Override
    public Boolean deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'boolean'", v.getType().name()));
        return (boolean) v.get();
    }

    @Override
    public Boolean deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'boolean'", v.getType().name()));
        return (boolean) v.get();
    }
}
