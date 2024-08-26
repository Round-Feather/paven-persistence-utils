# Paven Persistence Utils - A Codeless Database Layer for Quarkus and Google Datastore

---

[![Quality Gate Status](https://sonar.v3.paven.io/api/project_badges/measure?project=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9&metric=alert_status&token=sqb_094d914fde6a0fa1158ba9b4206e227d0d4d199c)](https://sonar.v3.paven.io/dashboard?id=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9)
[![Coverage](https://sonar.v3.paven.io/api/project_badges/measure?project=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9&metric=coverage&token=sqb_094d914fde6a0fa1158ba9b4206e227d0d4d199c)](https://sonar.v3.paven.io/dashboard?id=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9)
[![Lines of Code](https://sonar.v3.paven.io/api/project_badges/measure?project=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9&metric=ncloc&token=sqb_094d914fde6a0fa1158ba9b4206e227d0d4d199c)](https://sonar.v3.paven.io/dashboard?id=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9)
[![Reliability Rating](https://sonar.v3.paven.io/api/project_badges/measure?project=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9&metric=reliability_rating&token=sqb_094d914fde6a0fa1158ba9b4206e227d0d4d199c)](https://sonar.v3.paven.io/dashboard?id=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9)
[![Maintainability Rating](https://sonar.v3.paven.io/api/project_badges/measure?project=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9&metric=sqale_rating&token=sqb_094d914fde6a0fa1158ba9b4206e227d0d4d199c)](https://sonar.v3.paven.io/dashboard?id=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9)
[![Bugs](https://sonar.v3.paven.io/api/project_badges/measure?project=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9&metric=bugs&token=sqb_094d914fde6a0fa1158ba9b4206e227d0d4d199c)](https://sonar.v3.paven.io/dashboard?id=Round-Feather_paven-persistence-utils_AZFQ_wKnmpqdTNJ_C8m9)

## Java and Quarkus Compatibility

<!-- markdownlint-disable -->
<table class="no-border">
    <tr>
        <td><img alt="Static Badge" src="https://img.shields.io/badge/JDK_Version-17-green?style=for-the-badge&logo=openjdk&logoColor=white"></td>
        <td><img alt="Static Badge" src="https://img.shields.io/badge/Quarkus_Version-3.7.3-green?style=for-the-badge&logo=quarkus&logoColor=white"></td>
    </tr>
</table>

---

**Paven Persistence Utils** is a Java library that integrates seamlessly with [Quarkus-ARC](https://quarkus.io/guides/cdi-reference) and [Google Datastore](https://cloud.google.com/datastore) to provide a powerful codeless database layer. By leveraging Quarkus's dependency injection and Google's NoSQL database, this library simplifies data management by reducing the boilerplate code required to interact with your datastore.

## Features

- **Codeless Database Operations:** Automatically handle CRUD operations without writing repetitive data access code.
- **Quarkus-ARC Integration:** Leverage Quarkus-ARC for dependency injection and lifecycle management.
- **Google Datastore Integration:** Use Google Datastore's scalable NoSQL database with minimal configuration.
- **Annotation-Driven:** Define entities and relationships using simple annotations.

## Installation

Add the following dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>com.roundfeather.paven</groupId>
    <artifactId>paven-persistence-utils</artifactId>
</dependency>
```

## Getting Started

### 1. Define Your Entities

Use simple annotations to define your entities:

```java
// Works with builders/getters/setters or public fields
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatastoreEntity("myEntity")
public class MyObject {

    // Keys can be omitted if you only need to read from the database
    // suitable for database configs
    @DatastoreKey(keyType = KeyType.LONG)
    private Long id;

    // Fields can be marked as ancestors, if you have multiple ancestors
    // you need to specify the order
    @DatastoreAncestor(keyType = KeyType.LONG, kind = "ancestor1kind")
    private Long ancestor1;
    @DatastoreAncestor(keyType = KeyType.LONG, kind = "ancestor2kind", order = 2)
    private Long ancestor2;

    // Fields can have different property names in datastore
    @DatastorePropertyAs("goodName")
    private List<String> badName;

    // Fields can be skipped with reading/writing from Datastore
    @DatastoreSkip
    private Double skipField;

    // Fields can be excluded from indexing
    @DatastoreExcludeFromIndex
    private Map<String, Map<Integer, Double>> skipIndex;

    // Objects in a different entity can also be aggregated
    // the ancestorFactory interface describes how ancestors should be
    // derived from the parent object
    @DatastoreExternalEntity(ancestorFactory = MyAncestorFactory.class)
    private List<MyExternalObject> externalObjects;
    
    // Datastore properties can be nested in a child object instead as well
    @DatastoreNested
    private SomeNestedObject nestedObject;

    // Fields not annotated will still get written/read from datastore
    private Double someField1;
    private Boolean someField2;
    private Long someField3;
    private Float someField4;

    // Embedded objects are also supported
    private MyOtherObject otherObject;
}
```

### 2. Inject and Use Repositories

Inject the repository to manage your entities:

```java
@ConfigProperty(name = "myObject.namespace")
String namespace;

@Inject
DatastoreRepository<MyObject> repository;

public MyObject createMyObject() {
    MyObject mo = MyObject.builder().build();

    // Returned object will have autogenerated ids for Key fields when
    // they have empty values in your object
    return repository.save(
            DatastoreNamespace.of(namespace),
            mo
    );
}
```

### 3. Querying the Datastore

Use the repository's built-in methods to query data:

```java

List<MyObject> myObjects = repository.list(
        DatastoreNamespace.of(namespace),
        Ancestor.of("ancestor1kind", "key"),
        Ancestor.of("ancestor2kind", 2L)
);

MyObject myObject = repository.list(
        DatastoreNamespace.of(namespace),
        Key.of("someKey"),
        Ancestor.of("ancestor1kind", "key"),
        Ancestor.of("ancestor2kind", 2L)
);

MyObject deletedObject = repository.delete(
        DatastoreNamespace.of(namespace),
        Key.of(1L),
        Ancestor.of("ancestor1kind", "key"),
        Ancestor.of("ancestor2kind", 2L)
);
```

### 4. Advanced Usage

## Mocking Repository

### 1. Injecting the Repository

A mock for a `DatastoreRepository` can be injected like any other object.

```java
@InjectMock
DatastoreRepository<MyObject> repository;
```

### 2. Mocking Calls

Mockito can easily mock method calls for the repository. The objects `DatastoreNamespace`, `Key` and `Ancestor` all have
custom `equal()` methods based on their field values which let you use Mockito's `eq()` matcher even when they are created
in the method under test.

> ⚠️ **_NOTE:_**
> `DatastoreRepository` has methods that take lists or varargs as arguments. Keep in mind to mock the specific signature
> that you are using in your code

```java
// List argument example
when(repository.list(any(), (List<Ancestor>) any()))
        .thenReturn(List.of(myObject1, myObject2));

// Varargs argument examples
when(repository.find(any(), eq(Key.of(1L)), (Ancestor[]) any()))
        .thenReturn(myObject3);

when(repository.find(any(), eq(Key.of(1L)), eq(Ancestor.of("ancestor1Kind", 1L))))
        .thenReturn(myObject4);
```

## Comparison Utilities

### 1. *isSame()*

A helper function is included that will compare The two objects and determine if they are the same or not.

> ⚠️ **_NOTE:_**
> Currently `isSame()` does not compare the values in a `Map`

### 2. *mergeNonNullFields()*

A helper function is included will merge all the non-null fields from a source object into a target object.

> ⚠️ **_NOTE:_**
> Currently `mergeNonNullFields()` does not merge the values in a `Map`
