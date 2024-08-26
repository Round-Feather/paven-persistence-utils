package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.utils.datastore.DatastoreNamespace;
import com.roundfeather.persistence.utils.datastore.EntityManager;
import com.roundfeather.persistence.utils.datastore.serde.DataStoreObjectSerde;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@Startup
@ApplicationScoped
@Slf4j
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
        log.debug(String.format("Serializing '%s' to a value of type 'STRING'", o.getClass().getName()));
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
        log.debug(String.format("Deserializing value type '%s' as a 'String'", v.getType().name()));
        return (String) v.get();
    }

    @Override
    public String deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'String'", v.getType().name()));
        return (String) v.get();
    }
}
