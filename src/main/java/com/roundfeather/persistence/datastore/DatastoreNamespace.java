package com.roundfeather.persistence.datastore;

import lombok.Getter;

@Getter
public class DatastoreNamespace {

    private String project;
    private String namespace;

    public static DatastoreNamespace of(String project, String namespace) {
        return new DatastoreNamespace(project, namespace);
    }

    public static DatastoreNamespace of(String namespace) {
        return new DatastoreNamespace(null, namespace);
    }


    private DatastoreNamespace(String project, String namespace) {
        this.project = project;
        this.namespace = namespace;
    }
}
