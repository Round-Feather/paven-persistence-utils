package com.roundfeather.persistence.utils.datastore;

import com.roundfeather.persistence.utils.datastore.annotation.DatastoreAncestor;
import com.roundfeather.persistence.utils.datastore.annotation.KeyType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a Datastore ancestor
 *
 * @since 1.0
 */
@Getter
@Data
@EqualsAndHashCode
public class Ancestor {

    String kind;
    KeyType type;
    String name;
    Long id;

    /**
     * Creates a new String key based ancestor
     *
     * @param kind The name of the kind of the ancestor
     * @param name The name value of the ancestor
     * @return The new String key ancestor
     *
     * @since 1.0
     */
    public static Ancestor of(String kind, String name) {
        return new Ancestor(kind, KeyType.STRING, name, 0L);
    }

    /**
     * Creates a new Long key based ancestor
     *
     * @param kind The name of the kind of the ancestor
     * @param id The id value of the ancestor
     * @return The new Long key ancestor
     *
     * @since 1.0
     */
    public static Ancestor of(String kind, Long id) {
        return new Ancestor(kind, KeyType.LONG, "", id);
    }

    /**
     * Create a new Ancestor from a {@link DatastoreAncestor}
     *
     * @param ancestorAnnotation The annotation
     * @param value The value of the ancestor
     * @return The new ancestor
     */
    protected static Ancestor of(DatastoreAncestor ancestorAnnotation, Object value) {
        if (ancestorAnnotation.keyType() == KeyType.LONG) {
            return new Ancestor(ancestorAnnotation.kind(), ancestorAnnotation.keyType(), "", (Long) value);
        } else {
            return new Ancestor(ancestorAnnotation.kind(), ancestorAnnotation.keyType(), (String) value, 0L);
        }
    }

    /**
     * Ancestor constructor
     *
     * @param kind The name of the kind of the ancestor
     * @param type The type of ancestor key
     * @param name The name value of the ancestor for String keys
     * @param id The id value of the ancestor for Long keys
     *
     * @since 1.0
     */
    private Ancestor(String kind, KeyType type, String name, Long id) {
        this.kind = kind;
        this.type = type;
        this.name = name;
        this.id = id;
    }
}
