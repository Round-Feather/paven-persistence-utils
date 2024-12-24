package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.datastore.ListValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.utils.datastore.DatastoreNamespace;
import com.roundfeather.persistence.utils.datastore.EntityManager;
import com.roundfeather.persistence.utils.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.3
 */
@ApplicationScoped
@Slf4j
@SuppressWarnings({"squid:S3740"})
public class SetSerde implements DataStoreObjectSerde<Set> {

    @Override
    public Class<Set> getType() {
        return Set.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof Set;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        log.debug(String.format("Serializing '%s' to a value of type 'SET'", o.getClass().getName()));
        return ListValue.newBuilder()
                .set(((Set) o).stream()
                        .map(li -> em.createProperty(li, false))
                        .toList()
                )
                .build();
    }

    @Override
    public boolean canDeserialize(Type t, Value v) {
        try {
            return ((ParameterizedType) t).getRawType() == Set.class && v.getType() == ValueType.LIST;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canDeserialize(Class t, Value v) {
        return t == Set.class && v.getType() == ValueType.LIST;
    }

    @Override
    public Set deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        log.debug(String.format("Deserializing value type '%s' as a 'java.util.Set'", v.getType().name()));
        Set<Object> o = new HashSet<>();
        Type etp = ((ParameterizedType) tp).getActualTypeArguments()[0];

        ((List) v.get()).forEach(
                vi -> {
                    if (etp.getClass() == Class.class) {
                        o.add(em.handleProperty(dsNamespace, (Class) etp, (Value) vi));
                    } else {
                        o.add(em.handleProperty(dsNamespace, etp, (Value) vi));
                    }
                }
        );
        return o;
    }

    @Override
    public Set deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return Set.of();
    }
}
