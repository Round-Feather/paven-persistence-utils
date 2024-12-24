package com.roundfeather.persistence.utils.datastore;

import com.google.cloud.datastore.*;
import com.roundfeather.persistence.utils.datastore.annotation.DatastoreEntity;
import com.roundfeather.persistence.utils.datastore.annotation.DatastoreKey;
import com.roundfeather.persistence.utils.datastore.annotation.KeyType;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Generalized proxy for Datastore
 *
 * @since 1.0
 */
@ApplicationScoped
@SuppressWarnings({"PMD.UnnecessaryFullyQualifiedName", "squid:S3740"})
public class DatastoreOperations {


    private static DatastoreOperations instance;

    private final EntityManager entityManager;

    @Getter
    private final Datastore datastore;

    private DatastoreOperations(Datastore datastore, EntityManager entityManager) {
        this.datastore = datastore;
        this.entityManager = entityManager;
    }

    /**
     * Retrieves an instance of {@link DatastoreOperations} from a static context
     *
     * @return The DatastoreOperations used for all interaction with datastore
     *
     * @since 1.0
     */
    public static DatastoreOperations getInstance() {
        if (instance == null) {
            instance = new DatastoreOperations(
                    Arc.container().instance(Datastore.class).get(),
                    Arc.container().instance(EntityManager.class).get()
            );
        }
        return instance;
    }

    /**
     * Queries the Datastore entity corresponding to the {@code value()} of the {@link DatastoreEntity} for the given class
     * to get the object corresponding to the given key and list of ancestors
     *
     * <p>
     *     Ancestors will be applied in the order provided when generating the Datastore query
     * </p>
     *
     * @param dsNamespace The namespace to query
     * @param tp The class of the object to return
     * @param key The unique key of the entity to find
     * @param ancestors List of ancestors to apply to the query
     * @return The found object
     *
     * @since 1.0
     */
    public <T> T get(DatastoreNamespace dsNamespace, Class<T> tp, Key key, List<Ancestor> ancestors) {
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

    /**
     * Queries the Datastore entity corresponding to the {@code value()} of the {@link DatastoreEntity} for the given class
     * to get all objects corresponding to the given list of ancestors
     *
     * <p>
     *     Ancestors will be applied in the order provided when generating the Datastore query
     * </p>
     *
     * @param dsNamespace The namespace to query
     * @param tp The class of the object to return
     * @param ancestors List of ancestors to apply to the query
     * @return List of found objects
     *
     * @since 1.0
     */
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

    /**
     * Saves an Object  to Datastore entity corresponding to the {@code value()} of the {@link DatastoreEntity} for the
     * class of the object with additional Ancestors not found in the object.
     *
     * <p>
     *     If the object does not have a field annotated with {@link DatastoreKey}, it cannot be serialized and written
     *     to Datastore
     * </p>
     *
     * @param dsNamespace Namespace to write the object to
     * @param o Object to save
     * @param additionalAncestors List of ancestors to include when in the Entity's key
     * @param prependAdditionalAncestors If the additional ancestors should be added before or after the ancestors in the object
     * @return The written object, including any autogenerated key fields
     *
     * @since 1.0
     */
    public Object persist(DatastoreNamespace dsNamespace, Object o, List<Ancestor> additionalAncestors, boolean prependAdditionalAncestors) {
        FullEntity e = entityManager.serialize(dsNamespace, o, additionalAncestors, prependAdditionalAncestors);
        Entity pe = datastore.put(e);
        return entityManager.deserialize(dsNamespace, pe, o.getClass());
    }

    /**
     * Deletes an object from Datastore entity corresponding to the {@code value()} of the {@link DatastoreEntity} for
     * the provided class corresponding to the given key and list of ancestors
     *
     * <p>
     *     Ancestors will be applied in the order provided when generating the Datastore query
     * </p>
     *
     * @param dsNamespace Namespace to delete object from
     * @param tp The class of the object to return
     * @param key The unique key of the entity to delete
     * @param ancestors List of ancestors for the entity
     * @return The deleted object
     *
     * @since 1.0
     */
    public <T> T delete(DatastoreNamespace dsNamespace, Class<T> tp, Key key, List<Ancestor> ancestors) {
        T o = get(dsNamespace, tp, key, ancestors);
        FullEntity e = entityManager.serialize(dsNamespace, o, List.of(), true);
        datastore.delete((com.google.cloud.datastore.Key) e.getKey());
        return o;
    }

    /**
     * Runs a custom Datastore query
     *
     * @param query Custom Datastore Query to run.
     * @param tp The class of the objects to return
     * @return List of objects that match the query
     *
     * @since 1.0
     */
    public <T> List<T> eval(Query query, Class<T> tp) {
        QueryResults<Entity> results = datastore.run(query);

        List<T> entities = new ArrayList<>();
        results.forEachRemaining(
                e -> entities.add(entityManager.deserialize(DatastoreNamespace.of(query.getNamespace()), e, tp))
        );

        return entities;
    }
}
