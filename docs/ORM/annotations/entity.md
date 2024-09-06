---
status: new
---

# DatastoreEntity

The `@DatastoreEntity` annotation is a key component in the custom Object-Relational Mapping (ORM) framework designed for Google Datastore. This annotation marks a class as a Datastore entity and provides additional configurations for automatic repository generation.

## Overview

The `@DatastoreEntity` annotation is used to designate a Java class as an entity that will be stored and managed in Google Datastore. By default, this annotation automatically generates a corresponding repository, simplifying database operations for the annotated class.

### Annotation Properties

- **`value`**: Specifies the name of the entity in Google Datastore.
- **`autoGenerateRepository`**: A boolean flag that determines if a `DatastoreRepository` implementation should be automatically generated. If set to `false`, the developer can implement a custom repository.

### Usage Example

To use the `@DatastoreEntity` annotation, simply annotate your class with it and provide the entity name:

```java
@DatastoreEntity("User") // (1)
public class User {
    private String id;
    private String name;

    // Getters and setters
}
```

1. The value has to match the entity in Datastore exactly

In this example, a `DatastoreRepository<User>` will be automatically generated, allowing you to interact with the `User` entity in Google Datastore.

#### Customizing Repository Generation

If you need to implement a more complex repository and don’t want an automatic repository, set `autoGenerateRepository` to `false`:

```java
@DatastoreEntity(value = "Order", autoGenerateRepository = false)
public class Order {
    private String orderId;
    private Date orderDate;

    // Getters and setters
}
```

In this case, you can define your custom repository logic instead of relying on the auto-generated one.

## Key Features

- **Entity Mapping**: Simplifies the mapping between Java objects and Datastore entities.
- **Automatic Repository Generation**: Automatically generates a repository for basic CRUD operations unless otherwise specified.
- **Customizability**: Offers the flexibility to disable automatic repository generation for complex use cases.

## Parameters

| Parameter                | Type      | Description                                                    | Default |
|--------------------------|-----------|----------------------------------------------------------------|---------|
| `value`                  | `String`  | Name of the entity in Google Datastore.                        | N/A     |
| `autoGenerateRepository`  | `boolean` | Determines if a repository is automatically generated.         | `true`  |

## Version Information

- **Since**: 1.0
