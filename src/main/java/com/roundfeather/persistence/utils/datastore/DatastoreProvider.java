package com.roundfeather.persistence.utils.datastore;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

/**
 * Provider bean to allow for the injection of {@link Datastore}
 *
 * @since 1.0
 */
@Dependent
public class DatastoreProvider {

    /**
     * Method for initializing a new {@link Datastore}
     *
     * @return The Datastore instance
     *
     * @since 1.0
     */
    @Startup
    @Produces
    @ApplicationScoped
    public Datastore getDatastore() {
        return DatastoreOptions.getDefaultInstance().getService();
    }
}
