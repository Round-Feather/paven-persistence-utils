package com.roundfeather.persistence.datastore;

import com.google.cloud.datastore.*;
import com.roundfeather.persistence.datastore.annotation.DatastoreEntity;
import com.roundfeather.persistence.datastore.annotation.KeyType;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
public class DatastoreOperations {


    private static DatastoreOperations instance;

    private final EntityManager entityManager;

    @Getter
    private final Datastore datastore;

    private DatastoreOperations(Datastore datastore, EntityManager entityManager) {
        this.datastore = datastore;
        this.entityManager = entityManager;
    }

    public static DatastoreOperations getInstance() {
        if (instance == null) {
            instance = new DatastoreOperations(
                    Arc.container().instance(Datastore.class).get(),
                    Arc.container().instance(EntityManager.class).get()
            );
        }
        return instance;
    }

    public <T> T get(DatastoreNamespace dsNamespace, Class<T> tp, com.roundfeather.persistence.datastore.Key key, List<Ancestor> ancestors) {
        EntityQuery.Builder builder = Query.newEntityQueryBuilder()
                .setKind(tp.getAnnotation(DatastoreEntity.class).value())
                .setNamespace(dsNamespace.getNamespace());

        List<PathElement> pathElements = new ArrayList<>();

        if (!ancestors.isEmpty()) {

            ancestors.stream()
                    .map(
                            a -> {
                                if (a.getType() == KeyType.LONG) {
                                    return PathElement.of(a.getKind(), a.getId());
                                } else {
                                    return PathElement.of(a.getKind(), a.getName());
                                }
                            }
                    )
                    .forEach(
                            pathElements::add
                    );
        }

        KeyFactory kf = datastore.newKeyFactory()
                .addAncestors(pathElements)
                .setNamespace(dsNamespace.getNamespace())
                .setKind(entityManager.getKind(tp));

        com.google.cloud.datastore.Key k;

        if (key.getType() == KeyType.LONG) {
            k = kf.newKey(key.getId());
        } else {
            k = kf.newKey(key.getName());
        }

        builder.setFilter(StructuredQuery.PropertyFilter.hasAncestor(k));

        QueryResults<Entity> results = datastore.run(builder.build());

        if (results.hasNext()) {
            return entityManager.deserialize(dsNamespace, results.next(), tp);
        } else {
            return null;
        }
    }

    public <T> List<T> list(DatastoreNamespace dsNamespace, Class<T> tp, List<Ancestor> ancestors) {
        EntityQuery.Builder builder = Query.newEntityQueryBuilder()
                .setKind(tp.getAnnotation(DatastoreEntity.class).value())
                .setNamespace(dsNamespace.getNamespace());

        if (!ancestors.isEmpty()) {

            List<PathElement> pathElements = new ArrayList<>(ancestors.stream()
                    .map(
                            a -> {
                                if (a.getType() == KeyType.LONG) {
                                    return PathElement.of(a.getKind(), a.getId());
                                } else {
                                    return PathElement.of(a.getKind(), a.getName());
                                }
                            }
                    )
                    .toList());

            PathElement lastPE = pathElements.get(pathElements.size() - 1);

            Object kv = lastPE.getNameOrId();

            pathElements.remove(pathElements.size() - 1);

            KeyFactory kf = datastore.newKeyFactory()
                    .addAncestors(pathElements)
                    .setNamespace(dsNamespace.getNamespace())
                    .setKind(lastPE.getKind());

            com.google.cloud.datastore.Key k;

            if (kv.getClass() == String.class) {
                k = kf.newKey((String) kv);
            } else {
                k = kf.newKey((Long) kv);
            }

            builder.setFilter(StructuredQuery.PropertyFilter.hasAncestor(k));
        }

        QueryResults<Entity> results = datastore.run(builder.build());

        List<T> entities = new ArrayList<>();
        results.forEachRemaining(
                e -> entities.add(entityManager.deserialize(dsNamespace, e, tp))
        );

        return entities;

    }

    public Object persist(DatastoreNamespace dsNamespace, Object o) {
        FullEntity e = entityManager.serialize(dsNamespace, o);
        Entity pe = datastore.put(e);
        return entityManager.deserialize(dsNamespace, pe, o.getClass());
    }

    public <T> T delete(DatastoreNamespace dsNamespace, Class<T> tp, com.roundfeather.persistence.datastore.Key key, List<Ancestor> ancestors) {
        T o = get(dsNamespace, tp, key, ancestors);
        FullEntity e = entityManager.serialize(dsNamespace, o);
        datastore.delete((com.google.cloud.datastore.Key) e.getKey());
        return o;
    }

    public <T> List<T> eval(Query query, Class<T> tp) {
        QueryResults<Entity> results = datastore.run(query);

        List<T> entities = new ArrayList<>();
        results.forEachRemaining(
                e -> entities.add(entityManager.deserialize(DatastoreNamespace.of(query.getNamespace()), e, tp))
        );

        return entities;
    }
}
