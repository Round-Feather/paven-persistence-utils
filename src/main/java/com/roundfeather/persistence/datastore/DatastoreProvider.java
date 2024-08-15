package com.roundfeather.persistence.datastore;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import io.quarkus.arc.DefaultBean;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class DatastoreProvider {

    @Startup
    @Produces
    @DefaultBean
    @ApplicationScoped
    public Datastore getDatastore() {
        return DatastoreOptions.getDefaultInstance().getService();
    }
}
