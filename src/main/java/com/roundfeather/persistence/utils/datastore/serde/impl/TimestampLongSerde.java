package com.roundfeather.persistence.utils.datastore.serde.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.TimestampValue;
import com.google.cloud.datastore.Value;
import com.roundfeather.persistence.utils.datastore.EntityManager;
import com.roundfeather.persistence.utils.datastore.serde.CustomSerde;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @see CustomSerde
 *
 * @since 1.3
 */
@ApplicationScoped
public class TimestampLongSerde implements CustomSerde<Long> {

    @Override
    public Value serialize(EntityManager em, Long o, boolean excludeFromIndex) {
        return TimestampValue
                .newBuilder(Timestamp.ofTimeMicroseconds(o))
                .setExcludeFromIndexes(excludeFromIndex)
                .build();
    }

    @Override
    public Long deserialize(EntityManager em, Value v) {
        return ((TimestampValue) v).get().toSqlTimestamp().getTime();
    }
}
