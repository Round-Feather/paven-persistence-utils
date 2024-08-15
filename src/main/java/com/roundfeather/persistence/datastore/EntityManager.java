package com.roundfeather.persistence.datastore;

import static com.google.cloud.datastore.FullEntity.newBuilder;

import com.google.cloud.datastore.*;
import com.roundfeather.persistence.datastore.annotation.*;
import com.roundfeather.persistence.datastore.serde.impl.EntitySerde;
import com.roundfeather.persistence.datastore.serde.DataStoreObjectSerde;
import io.quarkus.arc.All;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.*;

@Startup
@ApplicationScoped
@SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
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

    public FullEntity serialize(DatastoreNamespace dsNamespace, Object o) {
        IncompleteKey key = createKey(dsNamespace, o);

        return createEntity(o, Optional.of(key));
    }

    private IncompleteKey createKey(DatastoreNamespace dsNamespace, Object o) {
        KeyFactory keyFactory;

        if (dsNamespace.getProject() == null) {
            keyFactory = datastore.newKeyFactory()
                    .setNamespace(dsNamespace.getNamespace())
                    .setKind(o.getClass().getAnnotation(DatastoreEntity.class).value());
        } else {
            keyFactory = datastore.newKeyFactory()
                    .setProjectId(dsNamespace.getProject())
                    .setNamespace(dsNamespace.getNamespace())
                    .setKind(o.getClass().getAnnotation(DatastoreEntity.class).value());
        }

        Map<Integer, Tuple2<DatastoreAncestor, Object>> ancestors = new HashMap<>();

        List<Tuple2<DatastoreKey, Object>> idFields = new ArrayList<>();

        Arrays.stream(o.getClass().getDeclaredFields())
                .forEach(
                        f -> {
                            DatastoreAncestor ancestor = f.getAnnotation(DatastoreAncestor.class);
                            if (ancestor != null) {
                                ancestors.put(ancestor.order(), Tuple2.of(ancestor, getFieldValue(o, f)));
                            }

                            DatastoreKey key = f.getAnnotation(DatastoreKey.class);
                            if (key != null) {
                                idFields.add(Tuple2.of(key, getFieldValue(o, f)));
                            }
                        }
                );

        List<Integer> ancestorsIds = new ArrayList<>(ancestors.keySet());
        Collections.sort(ancestorsIds);

        ancestorsIds.forEach(
                i -> {
                    if (ancestors.get(i).getItem2() != null) {
                        if (ancestors.get(i).getItem1().keyType() == KeyType.LONG) {
                            keyFactory.addAncestor(PathElement.of(
                                    ancestors.get(i).getItem1().kind(), (Long) ancestors.get(i).getItem2()
                            ));
                        } else {
                            keyFactory.addAncestor(PathElement.of(
                                    ancestors.get(i).getItem1().kind(), (String) ancestors.get(i).getItem2()
                            ));
                        }
                    }
                }
        );


        if (idFields.get(0).getItem1().keyType() == KeyType.LONG) {
            if ((long) idFields.get(0).getItem2() != 0L) {
                return keyFactory.newKey((long) idFields.get(0).getItem2());
            }
            return keyFactory.newKey();
        } else {
            if (!idFields.get(0).getItem2().equals("") && idFields.get(0).getItem2() != null) {
                return keyFactory.newKey((String) idFields.get(0).getItem2());
            }
            return keyFactory.newKey(UUID.randomUUID().toString());
        }
    }

    public Value createProperty(Object o, boolean excludeFromIndex) {
        DataStoreObjectSerde<?> relevantSerde = serdes.stream()
                .filter(s -> s.getType() != defaultSerde.getType())
                .filter(s -> s.canSerialize(o))
                .findFirst()
                .orElse(defaultSerde);

        return relevantSerde.serialize(this, o, excludeFromIndex);
    }

    public Value createProperty(Object o, Field f) {
        Object v = getFieldValue(o, f);

        if (v == null) {
            return null;
        }

        boolean excludeFromIndex = f.getAnnotation(DatastoreExcludeFromIndex.class) != null;

        return createProperty(v, excludeFromIndex);
    }

    private FullEntity createEntity(Object o, Optional<IncompleteKey> key) {
        FullEntity.Builder builder;
        if (key.isPresent()) {
            builder = newBuilder(key.get());
        } else {
            builder = newBuilder();
        }

        Arrays.stream(o.getClass().getDeclaredFields())
                .filter(f ->
                        f.getAnnotation(DatastoreKey.class) == null &&
                        f.getAnnotation(DatastoreAncestor.class) == null &&
                        f.getAnnotation(DatastoreExternalEntity.class) == null
                )
                .forEach(f -> {
                    Value v = createProperty(o, f);
                    if (v != null) {
                        builder.set(f.getName(), v);
                    }
                }
        );

        return builder.build();
    }

    private Object getFieldValue(Object o, Field f) {
        if (f.canAccess(o)) {
            try {
                return f.get(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            String getMethodName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            Optional<Method> getMethod = Arrays.stream(o.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals(getMethodName))
                    .findFirst();

            if (!getMethod.isEmpty() && getMethod.get().canAccess(o)) {
                try {
                    return getMethod.get().invoke(o);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        }
    }

    public <T> T deserialize(DatastoreNamespace dsNamespace, FullEntity e, Class<T> tp) {
        return createObject(dsNamespace, e, tp);
    }

    public <T> T handleProperty(DatastoreNamespace dsNamespace, Type t, Value v) {
        DataStoreObjectSerde<?> relevantSerde = serdes.stream()
                .filter(s -> s.getType() != defaultSerde.getType())
                .filter(s -> s.canDeserialize(t, v))
                .findFirst()
                .orElse(defaultSerde);

        return (T) relevantSerde.deserialize(dsNamespace, this, v, t);
    }

    public <T> T handleProperty(DatastoreNamespace dsNamespace, Class t, Value v) {
        DataStoreObjectSerde<?> relevantSerde = serdes.stream()
                .filter(s -> s.getType() != defaultSerde.getType())
                .filter(s -> s.canDeserialize(t, v))
                .findFirst()
                .orElse(defaultSerde);

        return (T) relevantSerde.deserialize(dsNamespace, this, v, t);
    }

    private <T> T createObject(DatastoreNamespace dsNamespace, FullEntity e, Class<T> tp) {
        Value v = EntityValue.of(e);

        return handleProperty(dsNamespace, tp, v);
    }

    public void setKeyFields(Object o, FullEntity e) {
        if (e.getKey() != null) {
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
    }

    public void setFieldValue(Object o, Field f, Object v) {
        if (f.canAccess(o)) {
            try {
                f.set(o, v);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            String setMethodName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            Optional<Method> setMethod = Arrays.stream(o.getClass().getDeclaredMethods())
                    .filter(m -> m.getName().equals(setMethodName))
                    .findFirst();

            if (!setMethod.isEmpty() && setMethod.get().canAccess(o)) {
                try {
                    setMethod.get().invoke(o, v);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

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

    public String getKind(Class tp) {
        return ((DatastoreEntity) tp.getAnnotation(DatastoreEntity.class)).value();
    }
}
