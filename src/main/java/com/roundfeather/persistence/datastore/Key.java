package com.roundfeather.persistence.datastore;

import com.roundfeather.persistence.datastore.annotation.KeyType;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class Key {

    KeyType type;
    String name;
    Long id;

    public static Key of(String name) {
        return new Key(KeyType.STRING, name, 0L);
    }

    public static Key of(Long id) {
        return new Key(KeyType.LONG, "", id);
    }

    private Key(KeyType type, String name, Long id) {
        this.type = type;
        this.name = name;
        this.id = id;
    }
}
