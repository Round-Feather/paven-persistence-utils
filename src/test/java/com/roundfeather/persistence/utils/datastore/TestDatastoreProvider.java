package com.roundfeather.persistence.utils.datastore;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import io.quarkus.arc.DefaultBean;
import io.quarkus.runtime.Startup;
import io.quarkus.test.Mock;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
@Slf4j
public class TestDatastoreProvider {

    private static LocalDatastoreHelper localDatastoreHelper;

    @PostConstruct
    void setup() {
        localDatastoreHelper = LocalDatastoreHelper.newBuilder().setConsistency(1).setStoreOnDisk(false).build();
        try {
            localDatastoreHelper.start();
            log.info("[Datastore-Emulator] listening on port: " + localDatastoreHelper.getPort());
            System.setProperty("DATASTORE_EMULATOR_HOST", "localhost:" + localDatastoreHelper.getPort());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Startup
    @Alternative
    @Priority(1)
    @Produces
    @ApplicationScoped
    public Datastore getDatastore() {
        return DatastoreOptions.getDefaultInstance().getService();
    }

    @PreDestroy
    void destroy() {
        try {
            localDatastoreHelper.stop();
            log.info("[Datastore-Emulator] stopped");
        } catch (IOException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
