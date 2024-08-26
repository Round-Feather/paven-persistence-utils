package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueType;
import com.roundfeather.persistence.utils.datastore.DatastoreNamespace;
import com.roundfeather.persistence.utils.datastore.EntityManager;
import com.roundfeather.persistence.utils.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

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
@SuppressWarnings({"PMD.CyclomaticComplexity"})
@Slf4j
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
        log.debug(String.format("Serializing '%s' to a value of type 'ENTITY'", o.getClass().getName()));

        FullEntity.Builder builder = FullEntity.newBuilder();

        ((Map) o).keySet().forEach(
                k -> {
                    log.debug(String.format("Found entry with key '%s'", k.toString()));

                    builder.set(k.toString(), em.createProperty(((Map) o).get(k), false));
                }
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
        log.debug(String.format("Deserializing value type '%s' as a 'java.util.Map'", v.getType().name()));

        Map o = new HashMap();
        Type ktp = ((ParameterizedType) tp).getActualTypeArguments()[0];
        Type vtp = ((ParameterizedType) tp).getActualTypeArguments()[1];

        ((EntityValue) v).get().getProperties()
                .forEach((ki, vi) -> {
                    Object k = getKey(ktp, ki);

                    try {
                        log.debug(String.format("Found '%s' property '%s'", vi.getType().name(), ki));

                        Object mv;
                        if (vtp.getClass() == Class.class) {
                            mv = em.handleProperty(dsNamespace, (Class) vtp, vi);
                        } else {
                            mv = em.handleProperty(dsNamespace, vtp, vi);
                        }

                        o.put(k, mv);
                    } catch (Exception e) {
                        log.warn(String.format("Property '%s' is not of type '%s'", ki, vtp.getTypeName()));
                    }
                }
        );

        return o;
    }

    @Override
    public Map deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        return Map.of();
    }

    /**
     * Converts the string key to the proper type
     *
     * @param ktp Type of the key field
     * @param kv String representation of the key
     * @return Converted key value
     */
    private static Object getKey(Type ktp, String kv) {
        Object k = null;
        if (ktp == String.class) {
            k = kv;
        } else if (ktp == Long.class || ktp == long.class) {
            k = Long.parseLong(kv);
        } else if (ktp == Integer.class || ktp == int.class) {
            k = Integer.parseInt(kv);
        } else if (ktp == Float.class || ktp == float.class) {
            k = Float.parseFloat(kv);
        } else if (ktp == Double.class || ktp == double.class) {
            k = Double.parseDouble(kv);
        } else if (ktp == Boolean.class || ktp == boolean.class) {
            k = Boolean.parseBoolean(kv);
        }

        return k;
    }
}
