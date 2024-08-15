package com.roundfeather.persistence.datastore.serde.impl;

import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.datastore.DatastoreNamespace;
import com.roundfeather.persistence.datastore.EntityManager;
import com.roundfeather.persistence.datastore.serde.DataStoreObjectSerde;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.reflect.Type;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@Startup
@ApplicationScoped
public class StringSerde implements DataStoreObjectSerde<String> {

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o.getClass() == String.class;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        return StringValue
                .newBuilder((String) o)
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type t, Value v) {
        return t == String.class && v.getType() == ValueType.STRING;
    }

    @Override
    public boolean canDeserialize(Class t, Value v) {
        return t == String.class && v.getType() == ValueType.STRING;
    }

    @Override
    public String deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        return (String) v.get();
    }

    @Override
    public String deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return (String) v.get();
    }
}
