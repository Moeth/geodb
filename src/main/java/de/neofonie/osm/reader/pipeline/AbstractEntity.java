package de.neofonie.osm.reader.pipeline;

import org.apache.commons.collections4.CollectionUtils;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractEntity<T extends Entity> implements Identifiable {

    private static final Logger log = LoggerFactory.getLogger(AbstractEntity.class);
    public static final List<String> HIGHWAY_RELEVANT = Arrays.asList("primary", "secondary", "tertiary", "unclassified", "residential", "service",
            "living_street", " pedestrian", "bus_guideway", "road");

    private final long id;

    AbstractEntity(T value) {
        id = value.getId();
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractEntity that = (AbstractEntity) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ id >>> 32);
    }

    public boolean isStreet() {
        if (this instanceof Way) {
            Way way = (Way) this;
            final List<String> highway = way.getTags("highway");
            return CollectionUtils.containsAny(highway, HIGHWAY_RELEVANT)
                    && way.getName() != null;
//            highway.remove("traffic_signals");
//            if (!highway.isEmpty()) {
//                log.info("highway " + this);
//            }
        }
        return false;
    }
}
