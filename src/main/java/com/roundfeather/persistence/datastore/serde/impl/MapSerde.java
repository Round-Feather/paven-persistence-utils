package com.roundfeather.persistence.datastore.serde.impl;

import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.datastore.DatastoreNamespace;
import com.roundfeather.persistence.datastore.EntityManager;
import com.roundfeather.persistence.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@ApplicationScoped
public class MapSerde implements DataStoreObjectSerde<Map> {

    @Override
    public Class<Map> getType() {
        return Map.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof Map;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        FullEntity.Builder builder = FullEntity.newBuilder();

        ((Map) o).keySet().forEach(
                k -> builder.set(k.toString(), em.createProperty(((Map) o).get(k), false))
        );

        return EntityValue
                .newBuilder(builder.build())
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type t, Value v) {
        try {
            return ((ParameterizedType) t).getRawType() == Map.class && v.getType() == ValueType.ENTITY;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canDeserialize(Class t, Value v) {
        return  t == Map.class && v.getType() == ValueType.ENTITY;
    }

    @Override
    public Map deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        Map o = new HashMap();
        Type ktp = ((ParameterizedType) tp).getActualTypeArguments()[0];
        Type vtp = ((ParameterizedType) tp).getActualTypeArguments()[1];

        ((EntityValue) v).get().getProperties().forEach(
                (ki, vi) -> {
                    Object k = null;
                    if (ktp == String.class) {
                        k = ki;
                    } else if (ktp == Long.class || ktp == long.class) {
                        k = Long.parseLong(ki);
                    } else if (ktp == Integer.class || ktp == int.class) {
                        k = Integer.parseInt(ki);
                    } else if (ktp == Float.class || ktp == float.class) {
                        k = Float.parseFloat(ki);
                    } else if (ktp == Double.class || ktp == double.class) {
                        k = Double.parseDouble(ki);
                    } else if (ktp == Boolean.class || ktp == boolean.class) {
                        k = Boolean.parseBoolean(ki);
                    }

                    if (vtp.getClass() == Class.class) {
                        o.put(k, em.handleProperty(dsNamespace, (Class) vtp, vi));
                    } else {
                        o.put(k, em.handleProperty(dsNamespace, (Type) vtp, vi));
                    }
                }
        );

        return o;
    }

    @Override
    public Map deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return Map.of();
    }
}
