package com.roundfeather.persistence.utils.datastore;

import static com.roundfeather.persistence.utils.ObjectUtils.getFieldValue;
import static com.roundfeather.persistence.utils.ObjectUtils.setFieldValue;

import com.google.cloud.datastore.*;
import com.roundfeather.persistence.utils.datastore.annotation.*;
import com.roundfeather.persistence.utils.datastore.serde.impl.EntitySerde;
import com.roundfeather.persistence.utils.datastore.serde.DataStoreObjectSerde;
import io.quarkus.arc.All;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.*;

/**
 * Entity Manager class for translating Datastore Entities to and from POJO objects
 *
 * @since 1.0
 */
@Startup
@ApplicationScoped
@SuppressWarnings({"PMD.UnnecessaryFullyQualifiedName", "PMD.CyclomaticComplexity"})
public class EntityManager {

    @Inject
    Datastore datastore;

    @Inject
    @All
    List<DataStoreObjectSerde<?>> serdes;

    @Inject
    @All
    List<ChildAncestorsFactory<?>> ancestorsFactories;

    @Inject
    EntitySerde defaultSerde;

    @Inject
    @All
    private List<DatastoreRepository<?>> repos;

    /**
     * Converts a POJO object to a Datastore {@link FullEntity}
     *
     * @param dsNamespace The namespace of the key
     * @param o The object to convert
     * @param additionalAncestors ist of ancestors to include when in the Entity's key
     * @param prependAdditionalAncestors If the additional ancestors should be added before or after the ancestors in the object
     * @return The entity representation of the object
     *
     * @since 1.0
     */
    public FullEntity serialize(DatastoreNamespace dsNamespace, Object o, List<Ancestor> additionalAncestors, boolean prependAdditionalAncestors) {
        IncompleteKey key = createKey(dsNamespace, o, additionalAncestors, prependAdditionalAncestors);

        return createEntity(o, key);
    }

    /**
     * Bootstraps the key with the project, namespace and kind
     *
     * @param dsNamespace The namespace and project of the key
     * @param kind The kind of the entity
     * @return The {@link KeyFactory} bootstrapped with the namespace and optionally project id
     *
     * @since 1.0
     */
    private KeyFactory bootstrapKeyFactory(DatastoreNamespace dsNamespace, String kind) {
        KeyFactory keyFactory;

        if (dsNamespace.getProject() == null) {
            keyFactory = datastore.newKeyFactory()
                    .setNamespace(dsNamespace.getNamespace())
                    .setKind(kind);
        } else {
            keyFactory = datastore.newKeyFactory()
                    .setProjectId(dsNamespace.getProject())
                    .setNamespace(dsNamespace.getNamespace())
                    .setKind(kind);
        }

        return keyFactory;
    }

    /**
     * Injects ancestors into the provided {@link KeyFactory} from the provided object and additional ancestors list
     *
     * @param keyFactory The KeyFactory to add ancestors too
     * @param o The object the extract ancestor fields from
     * @param additionalAncestors List of ancestors to include when in the Entity's key
     * @param prependAdditionalAncestors If the additional ancestors should be added before or after the ancestors in the object
     *
     * @since 1.0
     */
    private void addKeyAncestors(KeyFactory keyFactory, Object o, List<Ancestor> additionalAncestors, boolean prependAdditionalAncestors) {
        Map<Integer, Ancestor> ancestors = new HashMap<>();

        int ancestorOffset;
        if (prependAdditionalAncestors) {
            ancestorOffset = additionalAncestors.size();
        } else {
            ancestorOffset = 0;
        }

        Arrays.stream(o.getClass().getDeclaredFields())
                .forEach(
                        f -> {
                            DatastoreAncestor ancestorAnnotation = f.getAnnotation(DatastoreAncestor.class);
                            if (ancestorAnnotation != null) {
                                ancestors.put(ancestorAnnotation.order() + ancestorOffset, Ancestor.of(ancestorAnnotation, getFieldValue(o, f)));
                            }
                        }
                );

        int j = 1;
        int ancestorsCount = ancestors.size();

        for (Ancestor a: additionalAncestors) {
            int order = j;
            if (!prependAdditionalAncestors) {
                order = order + ancestorsCount;
            }

            ancestors.put(order, a);

            j = j + 1;
        }


        List<Integer> ancestorsIds = new ArrayList<>(ancestors.keySet());
        Collections.sort(ancestorsIds);

        ancestorsIds.forEach(
                i -> {
                    if (ancestors.get(i).getType() == KeyType.LONG) {
                        keyFactory.addAncestor(PathElement.of(
                                ancestors.get(i).getKind(), ancestors.get(i).getId()
                        ));
                    } else {
                        keyFactory.addAncestor(PathElement.of(
                                ancestors.get(i).getKind(), ancestors.get(i).getName()
                        ));
                    }
                }
        );
    }

    /**
     * Creates a key from a {@link KeyFactory} by injected the id from the object or generate the default if none found
     *
     * @param keyFactory KeyFactory to use to build the key
     * @param o Object potentially containing the key value
     * @return The Datastore key to use for the entity
     *
     * @since 1.0
     */
    private IncompleteKey addKeyValue(KeyFactory keyFactory, Object o) {
        List<Tuple2<DatastoreKey, Object>> idFields = new ArrayList<>();

        Arrays.stream(o.getClass().getDeclaredFields())
                .forEach(
                        f -> {
                            DatastoreKey key = f.getAnnotation(DatastoreKey.class);
                            if (key != null) {
                                idFields.add(Tuple2.of(key, getFieldValue(o, f)));
                            }
                        }
                );

        if (idFields.isEmpty()) {
            return keyFactory.newKey();
        } else {
            if (idFields.get(0).getItem1().keyType() == KeyType.LONG) {
                if (idFields.get(0).getItem2() != null && (long) idFields.get(0).getItem2() != 0L) {
                    return keyFactory.newKey((long) idFields.get(0).getItem2());
                }
                return keyFactory.newKey();
            } else {
                if (idFields.get(0).getItem2() != null && !idFields.get(0).getItem2().equals("")) {
                    return keyFactory.newKey((String) idFields.get(0).getItem2());
                }
                return keyFactory.newKey(UUID.randomUUID().toString());
            }
        }
    }

    /**
     * Creates a new Datastore Key with all ancestors, namespace abd id injected from the object and additional ancestors
     *
     * @param dsNamespace Namespace to write the object to
     * @param o Object being converted to an Entity
     * @param additionalAncestors List of ancestors to include when in the Entity's key
     * @param prependAdditionalAncestors If the additional ancestors should be added before or after the ancestors in the object
     * @return The Datastore key to use for the entity
     *
     * @since 1.0
     */
    private IncompleteKey createKey(DatastoreNamespace dsNamespace, Object o, List<Ancestor> additionalAncestors, boolean prependAdditionalAncestors) {
        KeyFactory keyFactory = bootstrapKeyFactory(dsNamespace, o.getClass().getAnnotation(DatastoreEntity.class).value());
        addKeyAncestors(keyFactory, o, additionalAncestors, prependAdditionalAncestors);
        return addKeyValue(keyFactory, o);
    }

    /**
     * Converts a POJO or primitive type into a Datastore {@link Value}
     *
     * @param o the object to convert
     * @param excludeFromIndex whether the value should be excluded from the index
     * @return The Value of an entity property
     *
     * @since 1.0
     */
    public Value createProperty(Object o, boolean excludeFromIndex) {
        DataStoreObjectSerde<?> relevantSerde = serdes.stream()
                .filter(s -> s.getType() != defaultSerde.getType())
                .filter(s -> s.canSerialize(o))
                .findFirst()
                .orElse(defaultSerde);

        return relevantSerde.serialize(this, o, excludeFromIndex);
    }

    /**
     * Converts a POJO or primitive type into a Datastore {@link Value}
     *
     * @param o Object to convert
     * @param f The Object's field to convert
     * @return The Value of an entity property
     *
     * @since 1.0
     */
    public Value createProperty(Object o, Field f) {
        Object v = getFieldValue(o, f);

        if (v == null) {
            return null;
        }

        boolean excludeFromIndex = f.getAnnotation(DatastoreExcludeFromIndex.class) != null;

        return createProperty(v, excludeFromIndex);
    }

    /**
     * Creates a Datastore {@link FullEntity} which can be written to Datastore
     *
     * @param o The POJO to convert
     * @param key the key for the entity
     * @return The Datastore entity
     *
     * @since 1.0
     */
    private FullEntity createEntity(Object o, IncompleteKey key) {
        EntityValue ev = (EntityValue) defaultSerde.serialize(this, o, false, key);
        return ev.get();
    }

    /**
     * Converts a Datastore Entity to a POJO
     *
     * @param dsNamespace Namespace of the entity
     * @param e The entity to convert
     * @param tp The type to convert the entity to
     * @return The POJO representation of the entity
     *
     * @since 1.0
     */
    public <T> T deserialize(DatastoreNamespace dsNamespace, FullEntity e, Class<T> tp) {
        return createObject(dsNamespace, e, tp);
    }

    /**
     * Converts a Datastore {@link Value} to a POJO or primitive type
     *
     * @param dsNamespace Namespace of the entity
     * @param t The type to convert the Value to
     * @param v The Value of the Datastore property
     * @return The POJO or primitive value of the Datastore property
     *
     * @since 1.0
     */
    public <T> T handleProperty(DatastoreNamespace dsNamespace, Type t, Value v) {
        DataStoreObjectSerde<?> relevantSerde = serdes.stream()
                .filter(s -> s.getType() != defaultSerde.getType())
                .filter(s -> s.canDeserialize(t, v))
                .findFirst()
                .orElse(defaultSerde);

        return (T) relevantSerde.deserialize(dsNamespace, this, v, t);
    }

    /**
     * Converts a Datastore {@link Value} to a POJO or primitive type
     *
     * @param dsNamespace Namespace of the entity
     * @param t The type to convert the Value to
     * @param v The Value of the Datastore property
     * @return The POJO or primitive value of the Datastore property
     *
     * @since 1.0
     */
    public <T> T handleProperty(DatastoreNamespace dsNamespace, Class t, Value v) {
        DataStoreObjectSerde<?> relevantSerde = serdes.stream()
                .filter(s -> s.getType() != defaultSerde.getType())
                .filter(s -> s.canDeserialize(t, v))
                .findFirst()
                .orElse(defaultSerde);

        return (T) relevantSerde.deserialize(dsNamespace, this, v, t);
    }

    /**
     * Converts a Datastore {@link FullEntity} to a POJO
     *
     * @param dsNamespace Namespace of the entity
     * @param e The entity to convert
     * @param tp The type of the POJO
     * @return The POJO object
     *
     * @since 1.0
     */
    private <T> T createObject(DatastoreNamespace dsNamespace, FullEntity e, Class<T> tp) {
        Value v = EntityValue.of(e);

        return handleProperty(dsNamespace, tp, v);
    }

    /**
     * Injects the values of the ancestors from the entity into the POJO
     *
     * @param o The POJO to inject ancestors in
     * @param e The Entity potentially with ancestors
     *
     * @since 1.0
     */
    private void setAncestorFields(Object o, FullEntity e) {
        e.getKey().getAncestors().forEach(
                pe -> {
                    Optional<Field> af = Arrays.stream(o.getClass().getDeclaredFields())
                            .filter(f -> f.getAnnotation(DatastoreAncestor.class) != null && f.getAnnotation(DatastoreAncestor.class).kind().equals(pe.getKind()))
                            .findFirst();

                    if (af.isPresent()) {
                        if (af.get().getAnnotation(DatastoreAncestor.class).keyType() == KeyType.LONG) {
                            setFieldValue(o, af.get(), pe.getId());
                        } else if (af.get().getAnnotation(DatastoreAncestor.class).keyType() == KeyType.STRING) {
                            setFieldValue(o, af.get(), pe.getName());
                        }
                    }
                }
        );
    }

    /**
     * Injects the values of the key from the entity into the POJO
     *
     * @param o The POJO to inject the key in
     * @param e The Entity with a key
     *
     * @since 1.0
     */
    private void setKeyField(Object o, FullEntity e) {
        Optional<Field> kf = Arrays.stream(o.getClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(DatastoreKey.class) != null)
                .findFirst();

        if (kf.isPresent()) {
            if (kf.get().getAnnotation(DatastoreKey.class).keyType() == KeyType.LONG) {
                setFieldValue(o, kf.get(), ((com.google.cloud.datastore.Key) e.getKey()).getId());
            } else if (kf.get().getAnnotation(DatastoreKey.class).keyType() == KeyType.STRING) {
                setFieldValue(o, kf.get(), ((com.google.cloud.datastore.Key) e.getKey()).getName());
            }
        }
    }

    /**
     * Injects the values of the key and ancestors from the entity into the POJO
     *
     * @param o The POJO to inject ancestors and key in
     * @param e The entity to extract key and ancestors from
     *
     * @since 1.0
     */
    public void setKeyFields(Object o, FullEntity e) {
        if (e.getKey() != null) {
            setAncestorFields(o, e);
            setKeyField(o, e);
        }
    }

    /**
     * Queries Datastore for a list of other entities to inject as a field
     *
     * @param dsNamespace Namespace of the entity
     * @param o The POJO to inject external entities in
     * @param f The POJO field to inject the external entites in
     * @return The List of POJOs to inject
     *
     * @since 1.0
     */
    public Object getExternalEntity(DatastoreNamespace dsNamespace, Object o, Field f) {
        if (f.getType() == List.class) {
            Class tp = (Class) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];

            Optional<DatastoreRepository<?>> repo = repos.stream()
                    .filter(
                            r -> {
                                Class rtp = (Class) ((ParameterizedType) ((Class) r.getClass().getGenericSuperclass()).getGenericInterfaces()[0]).getActualTypeArguments()[0];
                                return tp == rtp;
                            }
                    )
                    .findFirst();

            ChildAncestorsFactory af = Arc.container().instance(f.getAnnotation(DatastoreExternalEntity.class).ancestorFactory()).get();
            List<Ancestor> ancestors = af.buildChildAncestors(o);

            return repo.get().list(dsNamespace, ancestors);
        }

        return "";
    }

    /**
     * Gets the Datastore kind for a given class
     *
     * @param tp The class to get the kind for
     * @return The POJO's Datastore kind
     *
     * @since 1.0
     */
    public String getKind(Class tp) {
        return ((DatastoreEntity) tp.getAnnotation(DatastoreEntity.class)).value();
    }
}
