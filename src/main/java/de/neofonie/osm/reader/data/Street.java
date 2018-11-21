package de.neofonie.osm.reader.data;

import de.neofonie.osm.reader.pipeline.Way;
import de.neofonie.osm.reader.vector.Boundary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Street {

    private static final Logger log = LoggerFactory.getLogger(Street.class);
    private final Way way;
    private final Boundary boundary;

    public static Street create(Way way) {
        return new Street(way);
    }

    private Street(Way way) {
        this.way = way;
        boundary = Boundary.create(way);
    }

    public Boundary getBoundary() {
        return boundary;
    }

    public Way getWay() {
        return way;
    }
}
