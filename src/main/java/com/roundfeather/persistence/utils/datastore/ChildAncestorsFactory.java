package com.roundfeather.persistence.utils.datastore;

import java.util.List;

/**
 * Interface for deriving ancestors give a parent entity {@link P} to query Datastore and inject into {@link P}
 *
 * @param <P> Type of parent entity
 *
 * @since 1.0
 */
public interface ChildAncestorsFactory<P> {

    /**
     * Derive a list of ancestors to use to query Datastore to get the relevant child entities
     *
     * @param p The parent object to derive ancestors from
     * @return The list of ancestors to use when querying the child entity
     *
     * @since 1.0
     */
    List<Ancestor> buildChildAncestors(P p);
}
