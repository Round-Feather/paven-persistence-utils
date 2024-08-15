package com.roundfeather.persistence.datastore.serde.impl;

import com.google.cloud.datastore.EntityValue;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Value;
import com.roundfeather.persistence.datastore.DatastoreNamespace;
import com.roundfeather.persistence.datastore.EntityManager;
import com.roundfeather.persistence.datastore.annotation.*;
import com.roundfeather.persistence.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

import static com.google.cloud.datastore.FullEntity.newBuilder;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@ApplicationScoped
public class EntitySerde implements DataStoreObjectSerde<Object> {

    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    @Override
    public boolean canSerialize(Object o) {
        return true;
    }

    @Override
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex) {
        FullEntity.Builder builder = newBuilder();

        Arrays.stream(o.getClass().getDeclaredFields())
                .filter(f ->
                        f.getAnnotation(DatastoreKey.class) == null &&
                                f.getAnnotation(DatastoreAncestor.class) == null &&
                                f.getAnnotation(DatastoreExternalEntity.class) == null &&
                                f.getAnnotation(DatastoreSkip.class) == null
                )
                .forEach(f -> {
                            Value v = em.createProperty(o, f);
                            if (v != null) {
                                String propertyName;
                                if (f.getAnnotation(DatastorePropertyAs.class) != null) {
                                    propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
                                } else {
                                    propertyName = f.getName();
                                }
                                builder.set(propertyName, v);
                            }
                        }
                );

        return EntityValue
                .newBuilder(builder.build())
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public boolean canDeserialize(Type tp, Value v) {
        return true;
    }

    @Override
    public boolean canDeserialize(Class tp, Value v) {
        return true;
    }

    @Override
    public Object deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        Object o;
        FullEntity e = (FullEntity) v.get();

        try {
            Optional<Method> builderMethod = Arrays.stream(((Class) tp).getDeclaredMethods())
                    .filter(m -> m.getName().equals("builder"))
                    .findFirst();

            if (builderMethod.isPresent()) {
                Object builder = builderMethod.get().invoke(builderMethod.get());
                Method buildMethod = Arrays.stream(builder.getClass().getDeclaredMethods())
                        .filter(m -> m.getName().equals("build"))
                        .findFirst().get();

                o = buildMethod.invoke(builder);
            } else {
                o = ((Class) tp).getConstructor().newInstance();
            }

            em.setKeyFields(o, e);
            e.getProperties().keySet().forEach(
                    k -> {
                        Optional<Field> of = Arrays.stream(o.getClass().getDeclaredFields())
                                .filter(f -> {
                                    String propertyName;
                                    if (f.getAnnotation(DatastorePropertyAs.class) != null) {
                                        propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
                                    } else {
                                        propertyName = f.getName();
                                    }

                                    return propertyName.equals(k);
                                })
                                .filter(f -> f.getAnnotation(DatastoreSkip.class) == null)
                                .findFirst();

                        of.ifPresent(
                                f -> em.setFieldValue(o, f, em.handleProperty(dsNamespace, f.getGenericType(), (Value) e.getProperties().get(k)))
                        );
                    }
            );Arrays.stream(((Class) tp).getDeclaredFields())
                    .filter(f -> f.getAnnotation(DatastoreExternalEntity.class) != null)
                    .forEach(f -> em.setFieldValue(o, f, em.getExternalEntity(dsNamespace, o, f)));

        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        return o;
    }

    @Override
    public Object deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        Object o;
        FullEntity e = (FullEntity) v.get();

        try {
            Optional<Method> builderMethod = Arrays.stream(tp.getDeclaredMethods())
                    .filter(m -> m.getName().equals("builder"))
                    .findFirst();

            if (builderMethod.isPresent()) {
                Object builder = builderMethod.get().invoke(builderMethod.get());
                Method buildMethod = Arrays.stream(builder.getClass().getDeclaredMethods())
                        .filter(m -> m.getName().equals("build"))
                        .findFirst().get();

                o = buildMethod.invoke(builder);
            } else {
                o = tp.getConstructor().newInstance();
            }

            em.setKeyFields(o, e);
            e.getProperties().keySet().forEach(
                    k -> {
                        Optional<Field> of = Arrays.stream(o.getClass().getDeclaredFields())
                                .filter(f -> {
                                    String propertyName;
                                    if (f.getAnnotation(DatastorePropertyAs.class) != null) {
                                        propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
                                    } else {
                                        propertyName = f.getName();
                                    }

                                    return propertyName.equals(k);
                                })
                                .filter(f -> f.getAnnotation(DatastoreSkip.class) == null)
                                .findFirst();

                        of.ifPresent(
                                f -> em.setFieldValue(o, f, em.handleProperty(dsNamespace, f.getGenericType(), (Value) e.getProperties().get(k)))
                        );
                    }
            );

            Arrays.stream(tp.getDeclaredFields())
                    .filter(f -> f.getAnnotation(DatastoreExternalEntity.class) != null)
                    .forEach(f -> em.setFieldValue(o, f, em.getExternalEntity(dsNamespace, o, f)));

        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        return o;
    }
}
