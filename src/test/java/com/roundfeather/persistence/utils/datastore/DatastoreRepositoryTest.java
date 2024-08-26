package com.roundfeather.persistence.utils.datastore;

import com.google.cloud.datastore.Datastore;
import com.roundfeather.persistence.utils.datastore.model.TestObject;
import com.roundfeather.persistence.utils.datastore.model.TestObject2;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class DatastoreRepositoryTest {

    @Inject
    Datastore datastore;

    @Inject
    DatastoreRepository<TestObject> repository;

    @Test
    void someTest() {
        TestObject to = new TestObject();
        to.ancestor = "ancestor";
        to.to2 = TestObject2.builder()
                .l(10L)
                .s("S")
                .i(2)
                .d(3D)
                .f(0.3F)
                .ds(List.of(4D, 5D))
                .fs(Map.of("a", 0.6F))
                .b1(true)
                .build();

        repository.save(DatastoreNamespace.of("namespace"), to);
        List<TestObject> tos = repository.list(DatastoreNamespace.of("namespace"));

        assertEquals(1, tos.size());
    }
}
