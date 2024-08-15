package com.roundfeather.persistence.datastore;

import java.util.List;

public interface ChildAncestorsFactory<P> {

    List<Ancestor> buildChildAncestors(P p);
}
