package com.roundfeather.persistence.datastore.serde.impl;

import com.google.cloud.datastore.ListValue;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.datastore.DatastoreNamespace;
import com.roundfeather.persistence.datastore.EntityManager;
import com.roundfeather.persistence.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@ApplicationScoped
public class ListSerde implements DataStoreObjectSerde<List> {

    @Override
    public Class<List> getType() {
        return List.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof List;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        return ListValue.newBuilder()
                .set(((List) o).stream()
                        .map(li -> em.createProperty(li, false))
                        .toList()
                )
                .build();
    }

    @Override
    public boolean canDeserialize(Type t, Value v) {
        try {
            return ((ParameterizedType) t).getRawType() == List.class && v.getType() == ValueType.LIST;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canDeserialize(Class t, Value v) {
        return t == List.class && v.getType() == ValueType.LIST;
    }

    @Override
    public List deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        List<Object> o = new ArrayList<>();
        Type etp = ((ParameterizedType) tp).getActualTypeArguments()[0];

        ((List) v.get()).forEach(
                vi -> {
                    if (etp.getClass() == Class.class) {
                        o.add(em.handleProperty(dsNamespace, (Class) etp, (Value) vi));
                    } else {
                        o.add(em.handleProperty(dsNamespace, (Type) etp, (Value) vi));
                    }
                }
        );
        return o;
    }

    @Override
    public List deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return List.of();
    }
}
