package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.datastore.*;
import com.roundfeather.persistence.utils.datastore.DatastoreNamespace;
import com.roundfeather.persistence.utils.datastore.EntityManager;
import com.roundfeather.persistence.utils.datastore.annotation.*;
import com.roundfeather.persistence.utils.datastore.exceptions.PavenSerdeException;
import com.roundfeather.persistence.utils.datastore.serde.CustomSerde;
import com.roundfeather.persistence.utils.datastore.serde.DataStoreObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

import static com.google.cloud.datastore.FullEntity.newBuilder;
import static com.roundfeather.persistence.utils.ObjectUtils.*;

/**
 * @see DataStoreObjectSerde
 *
 * @since 1.0
 */
@ApplicationScoped
@Slf4j
@SuppressWarnings({"squid:S3740"})
public class EntitySerde implements DataStoreObjectSerde<Object> {

    private static final String FOUND_FIELD_AS = "Found '%s' field '%s' with name '%s'";
    private static final String FOUND_FIELD = "Found '%s' field '%s'";

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
        return serialize(em, o, excludeFromIndex, null);
    }

    /**
     * Bootstraps a new {@link FullEntity.Builder} with the provided key if not null
     *
     * @param key Key to add to the {@link FullEntity}, {@code null} if no key
     * @return The builder to use to create the entity
     *
     * @since 1.0
     */
    private FullEntity.Builder bootstrapBuilder(IncompleteKey key) {
        FullEntity.Builder builder;
        if (key != null) {
            builder = newBuilder(key);
        } else {
            builder = newBuilder();
        }

        return builder;
    }

    @Override
    @SuppressWarnings({"squid:S3776"})
    public Value serialize(EntityManager em, Object o, boolean excludeFromIndex, IncompleteKey key) {
        log.debug(String.format("Serializing '%s' to a value of type 'ENTITY'", o.getClass().getName()));

        FullEntity.Builder builder = bootstrapBuilder(key);

        getAllFields(o).stream()
                .filter(f ->
                        f.getAnnotation(DatastoreKey.class) == null &&
                                f.getAnnotation(DatastoreAncestor.class) == null &&
                                f.getAnnotation(DatastoreExternalEntity.class) == null &&
                                f.getAnnotation(DatastoreSkip.class) == null
                )
                .forEach(f -> {
                    if (f.getAnnotation(DatastorePropertyAs.class) != null) {
                        String propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
                        log.debug(String.format(FOUND_FIELD_AS, f.getType().getName(), f.getName(), propertyName));
                    } else {
                        log.debug(String.format(FOUND_FIELD, f.getType().getName(), f.getName()));
                    }
                    Value v;
                    if (f.getAnnotation(DatastoreWithSerde.class) != null) {
                        try {
                            CustomSerde<Object> customSerde = f.getAnnotation(DatastoreWithSerde.class).value().getDeclaredConstructor().newInstance();
                            v = customSerde.serialize(em, getFieldValue(o, f), f.getAnnotation(DatastoreExcludeFromIndex.class) != null);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException e) {
                            throw new PavenSerdeException(String.format("Failed serializing [%s] for [%s]", f.getName(), o.getClass().getName()), e);
                        }
                    } else {
                        v = em.createProperty(o, f);
                    }

                    if (v != null) {
                        if (f.getAnnotation(DatastoreNested.class) != null) {
                            EntityValue ev = (EntityValue) v;
                            Map<String, Value<?>> nestedProperties = ev.get().getProperties();
                            nestedProperties.forEach(builder::set);
                        } else {
                            String propertyName;
                            if (f.getAnnotation(DatastorePropertyAs.class) != null) {
                                propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
                            } else {
                                propertyName = f.getName();
                            }

                            builder.set(propertyName, v);
                        }
                    }
                });

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

    /**
     * Creates a new instance of the provided class, using either a builder or a no arguments constructor
     *
     * @param tp The type of object to create
     * @return The new object
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     *
     * @since 1.0
     */
    @SuppressWarnings({"squid:S3655", "squid:S3011"})
    private Object initializeObject(Class tp, FullEntity e) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Class tpToCreate = tp;
        if (tp.getAnnotation(DatastoreTypeInfo.class) != null) {
            tpToCreate = getSubClassImplementation(tp, e);
        }
        Optional<Method> builderMethod = Arrays.stream(tpToCreate.getDeclaredMethods())
                .filter(m -> m.getName().equals("builder"))
                .findFirst();

        if (builderMethod.isPresent()) {
            Object builder = builderMethod.get().invoke(builderMethod.get());
            Method buildMethod = Arrays.stream(builder.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals("build"))
                    .findFirst().get();
            buildMethod.setAccessible(true);
            return buildMethod.invoke(builder);
        } else {
            return tp.getConstructor().newInstance();
        }
    }

    /**
     * Checks if a {@link Field} should be deserialized
     *
     * @param f field to check
     * @return if the field can be filtered for deserialization
     *
     * @since 1.3
     */
    private static boolean fieldFilter(Field f) {
        return f.getAnnotation(DatastoreSkip.class) == null &&
                f.getAnnotation(DatastoreAncestor.class) == null &&
                f.getAnnotation(DatastoreKey.class) == null &&
                f.getAnnotation(DatastoreExternalEntity.class) == null &&
                f.getAnnotation(DatastoreSubTypes.class) == null;
    }

    /**
     * Checks if a property corresponds to a specfic {@link Field}
     *
     * @param f field to find property for
     * @param p name of property to check if corresponds to the field
     * @return if the property is for the specified field
     *
     * @since 1.3
     */
    private static boolean propertyFilter(Field f, String p) {
        String propertyName;
        if (f.getAnnotation(DatastorePropertyAs.class) != null) {
            propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
        } else {
            propertyName = f.getName();
        }
        return propertyName.equals(p);
    }

    /**
     * Sets the value of a {@link Field} based on a {@link FullEntity} property.
     *
     * @param f Field to set
     * @param p Name of datastore property
     * @param o Object to set the value for
     * @param em EntityManager for handling complex objects
     * @param dsNamespace Namespace of parent Entity
     * @param e Datastore entity deserializing
     *
     * @since 1.3
     */
    private static void handleProperty(Field f, String p, Object o, EntityManager em, DatastoreNamespace dsNamespace, FullEntity e) {
        if (f.getAnnotation(DatastorePropertyAs.class) != null) {
            String propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
            log.debug(String.format(FOUND_FIELD_AS, f.getType().getName(), f.getName(), propertyName));
        } else {
            log.debug(String.format(FOUND_FIELD, f.getType().getName(), f.getName()));
        }
        if (f.getAnnotation(DatastoreWithSerde.class) == null) {
            setFieldValue(o, f, em.handleProperty(dsNamespace, f.getGenericType(), (Value) e.getProperties().get(p)));
        } else {
            try {
                CustomSerde<?> customSerde = f.getAnnotation(DatastoreWithSerde.class).value().getDeclaredConstructor().newInstance();
                setFieldValue(o, f, customSerde.deserialize(em, (Value) e.getProperties().get(p)));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                throw new PavenSerdeException(String.format("Failed deserializing field [%s] for [%s]", f.getName(), o.getClass().getName()), ex);
            }
        }
    }

    /**
     * Sets all the fields of an object with values from a {@link FullEntity}
     *
     * @param o Object to set field values for
     * @param e Datastore entity to get values from
     * @param em EntityManager for handling complex objects
     * @param dsNamespace Namespace of parent Entity
     * @param v Datastore Value to deserialize
     *
     * @since 1.3
     */
    private static void handleProperties(Object o, FullEntity e, EntityManager em, DatastoreNamespace dsNamespace, Value v) {
        em.setKeyFields(o, e);

        getAllFields(o).stream()
                .filter(EntitySerde::fieldFilter)
                .forEach(f -> {
                            if (f.getAnnotation(DatastoreNested.class) != null) {
                                setFieldValue(o, f, em.handleProperty(dsNamespace, f.getGenericType(), v));
                            } else {
                                ((Stream<String>) e.getProperties().keySet().stream())
                                        .filter(p -> propertyFilter(f, p))
                                        .forEach(p -> handleProperty(f, p, o, em, dsNamespace, e));
                            }
                        }
                );

        getAllFields(o).stream()
                .filter(f -> f.getAnnotation(DatastoreExternalEntity.class) != null)
                .forEach(f -> setFieldValue(o, f, em.getExternalEntity(dsNamespace, o, f)));

        handleSubtypes(dsNamespace, em, e, o);
    }

    @Override
    public Object deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Type tp) {
        log.debug(String.format("Deserializing value type '%s' as a '%s'", v.getType().name(), ((Class) tp).getName()));

        Object o;
        FullEntity e = (FullEntity) v.get();

        try {
            o = initializeObject((Class) tp, e);
            handleProperties(o, e, em, dsNamespace, v);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new PavenSerdeException(String.format("Failed deserializing [%s]", ((Class) tp).getName()), ex);
        }

        return o;
    }

    @Override
    public Object deserialize(DatastoreNamespace dsNamespace, EntityManager em, Value v, Class tp) {
        log.debug(String.format("Deserializing value type '%s' as a '%s'", v.getType().name(), tp.getName()));

        Object o;
        FullEntity e = (FullEntity) v.get();

        try {
            o = initializeObject(tp, e);
            handleProperties(o, e, em, dsNamespace, v);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new PavenSerdeException(String.format("Failed deserializing [%s]", tp.getName()), ex);
        }

        return o;
    }

    /**
     * Handles the deserialization of a {@link FullEntity} into a subtype-able field
     *
     * @param dsNamespace Namespace of parent Entity
     * @param em EntityManager for handling complex objects
     * @param e Entity to deserialize
     * @param o Object to inject conditional subtype into
     *
     * @since 1.1
     */
    private static void handleSubtypes(DatastoreNamespace dsNamespace, EntityManager em, FullEntity e, Object o) {
        getAllFields(o).stream()
                .filter(f -> f.getAnnotation(DatastoreSubType.class) != null || f.getAnnotation(DatastoreSubTypes.class) != null)
                .forEach(f -> {
                    String pName = (String) (e.getProperties().keySet().stream())
                            .filter(p -> {
                                String propertyName;
                                if (f.getAnnotation(DatastorePropertyAs.class) != null) {
                                    propertyName = f.getAnnotation(DatastorePropertyAs.class).value();
                                } else {
                                    propertyName = f.getName();
                                }
                                return propertyName.equals(p);
                            })
                            .findFirst()
                            .orElse("");

                    if (!pName.isEmpty()) {
                        String subTypeName;
                        Value val = (Value) e.getProperties().get(pName);

                        if (f.getAnnotation(DatastoreTypeInfo.class).include() == InclusionType.EXTERNAL_PROPERTY) {
                            Field subTpField = getAllFields(o).stream()
                                    .filter(f2 -> f2.getName().equals(f.getAnnotation(DatastoreTypeInfo.class).property()))
                                    .findFirst()
                                    .get();

                            subTypeName = getFieldValue(o, subTpField).toString();
                        } else {
                            subTypeName = ((Value) e.getEntity(pName).getProperties().get(f.getAnnotation(DatastoreTypeInfo.class).property())).get().toString();
                        }

                        // TODO - make DatastoreSubTypes optional
                        Optional<Class> optionalSubTp = Arrays.stream(f.getAnnotation(DatastoreSubTypes.class).value())
                                .filter(st -> st.name().equals(subTypeName) || Arrays.stream(st.names()).toList().contains(subTypeName))
                                .map(DatastoreSubType::type)
                                .findFirst();

                        Class subTp;
                        subTp = optionalSubTp.orElseGet(
                                () -> f.getAnnotation(DatastoreTypeInfo.class).defaultImpl()
                        );

                        setFieldValue(o, f, em.handleProperty(dsNamespace, subTp, val));
                    }
                });
    }

    /**
     * Determine the correct subtype to initialize
     *
     * @param parentClass Class to get subtype implementation for
     * @param e Datastore entity to get subtype identifier value from
     * @return the subtype class to initialize
     *
     * @since 1.3
     */
    private static Class getSubClassImplementation(Class parentClass, FullEntity e) {
        DatastoreTypeInfo dti = (DatastoreTypeInfo) parentClass.getAnnotation(DatastoreTypeInfo.class);
        Optional<DatastoreSubTypes> dst = Optional.ofNullable((DatastoreSubTypes) parentClass.getAnnotation(DatastoreSubTypes.class));

        if (dti.include() == InclusionType.INTERNAL_PROPERTY) {
            @SuppressWarnings("squid:S3655")
            Field field = getAllFields(parentClass).stream()
                    .filter(f -> f.getName().equals(dti.property()))
                    .findFirst()
                    .get();

            String propertyName = dti.property();

            if (field.getAnnotation(DatastorePropertyAs.class) != null) {
                propertyName = field.getAnnotation(DatastorePropertyAs.class).value();
            }

            String type = e.getString(propertyName);

            Class subTp = dti.defaultImpl();

            if (dst.isPresent()) {
                Optional<Class> subTpImpl = Arrays.stream(dst.get().value())
                        .filter(st -> st.name().equals(type) || Arrays.stream(st.names()).toList().contains(type))
                        .map(DatastoreSubType::type)
                        .findFirst();

                if (subTpImpl.isPresent()) {
                    subTp = subTpImpl.get();
                }
            }

            return subTp;

        } else {
            throw new NotImplementedException();
        }
    }
}
