package com.roundfeather.persistence.utils.datastore.model.repository;

import com.google.cloud.datastore.Datastore;
import com.roundfeather.persistence.utils.datastore.DatastoreRepository;
import com.roundfeather.persistence.utils.datastore.model.TestObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TestObjectRepository implements DatastoreRepository<TestObject> {

    @Inject
    Datastore datastore;

}
