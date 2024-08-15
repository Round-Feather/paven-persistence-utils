package com.roundfeather.persistence.datastore;

import com.roundfeather.persistence.datastore.annotation.KeyType;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class Ancestor {

    String kind;
    KeyType type;
    String name;
    Long id;

    public static Ancestor of(String kind, String name) {
        return new Ancestor(kind, KeyType.STRING, name, 0L);
    }

    public static Ancestor of(String kind, Long id) {
        return new Ancestor(kind, KeyType.LONG, "", id);
    }

    private Ancestor(String kind, KeyType type, String name, Long id) {
        this.kind = kind;
        this.type = type;
        this.name = name;
        this.id = id;
    }
}
