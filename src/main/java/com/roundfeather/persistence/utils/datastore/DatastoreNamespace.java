package com.roundfeather.persistence.utils.datastore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents a Datastore namespace and project
 *
 * @since 1.0
 */
@Getter
@Data
@EqualsAndHashCode
public class DatastoreNamespace {

    private String project;
    private String namespace;

    /**
     * Creates a new project id and namespace {@link DatastoreNamespace}
     *
     * @param project The id of the project
     * @param namespace Then namespace
     * @return Then new namespace
     *
     * @since 1.0
     */
    public static DatastoreNamespace of(String project, String namespace) {
        return new DatastoreNamespace(project, namespace);
    }

    /**
     * Creates a new namespace {@link DatastoreNamespace}
     *
     * @param namespace Then namespace
     * @return Then new namespace
     *
     * @since 1.0
     */
    public static DatastoreNamespace of(String namespace) {
        return new DatastoreNamespace(null, namespace);
    }

    /**
     * DatastoreNamespace constructor
     *
     * @param project The id of the project
     * @param namespace Then namespace
     *
     * @since 1.0
     */
    private DatastoreNamespace(String project, String namespace) {
        this.project = project;
        this.namespace = namespace;
    }
}
