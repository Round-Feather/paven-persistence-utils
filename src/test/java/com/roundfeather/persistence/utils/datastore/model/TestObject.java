package com.roundfeather.persistence.utils.datastore.model;

import com.roundfeather.persistence.utils.datastore.annotation.DatastoreAncestor;
import com.roundfeather.persistence.utils.datastore.annotation.DatastoreEntity;
import com.roundfeather.persistence.utils.datastore.annotation.DatastoreKey;
import com.roundfeather.persistence.utils.datastore.annotation.KeyType;

@DatastoreEntity(value = "testObject", autoGenerateRepository = false)
public class TestObject {

    @DatastoreKey(keyType = KeyType.LONG)
    public Long id;

    @DatastoreAncestor(keyType = KeyType.STRING, kind = "a")
    public String ancestor;

    public TestObject2 to2;

    public TestObject() {}
}
