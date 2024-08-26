package com.roundfeather.persistence.utils.datastore;

import com.roundfeather.persistence.utils.datastore.annotation.KeyType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a Datastore key
 *
 * @since 1.0
 */
@Getter
@Data
@EqualsAndHashCode
public class Key {

    KeyType type;
    String name;
    Long id;

    /**
     * Creates a new String based key
     *
     * @param name The name value of the key
     * @return The new String key
     *
     * @since 1.0
     */
    public static Key of(String name) {
        return new Key(KeyType.STRING, name, 0L);
    }

    /**
     * Creates a new Long based key
     *
     * @param id The id value of the key
     * @return The new Long key
     *
     * @since 1.0
     */
    public static Key of(Long id) {
        return new Key(KeyType.LONG, "", id);
    }

    /**
     * Key constructor
     *
     * @param type The type of key
     * @param name The name value of the String key
     * @param id The id value of the Long key
     *
     * @since 1.0
     */
    private Key(KeyType type, String name, Long id) {
        this.type = type;
        this.name = name;
        this.id = id;
    }
}
