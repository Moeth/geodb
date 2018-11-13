package de.neofonie.osm.reader.vector;

import com.google.common.base.Preconditions;
import de.neofonie.osm.reader.pipeline.AbstractEntity;
import de.neofonie.osm.reader.pipeline.Relation;
import de.neofonie.osm.reader.pipeline.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Boundary implements Shape {

    private static final Logger log = LoggerFactory.getLogger(Boundary.class);
    private final Collection<Polygon> outerPolygons;
    private final Collection<Polygon> innerPolygons;
    private final Rectangle2D boundingBox;

    public static Boundary create(Relation relation) {
        final String type = relation.getTag("type");
        if ("multilinestring".equals(type)) {
            log.warn(String.format("multilinestring for %s not supported", relation));
            return null;
        }
        final List<AbstractEntity<?>> outer = relation.getMembers("outer");
        Preconditions.checkArgument(!outer.isEmpty(), String.format("ways for %d are empty", relation.getId()));
        final List<Polygon> outerPolygons = Polygon.createClosedPolygons(outer);
        final List<AbstractEntity<?>> inner = relation.getMembers("inner");
        final List<Polygon> innerPolygons = Polygon.createClosedPolygons(inner);
        return new Boundary(outerPolygons, innerPolygons);
    }

    public static Boundary create(Way way) {
        final Polygon outerPolygons = Polygon.createPolygon(way);
        return new Boundary(Collections.singleton(outerPolygons), Collections.emptyList());
    }

    private Boundary(Collection<Polygon> outerPolygons, Collection<Polygon> innerPolygons) {
        Preconditions.checkArgument(!outerPolygons.isEmpty());
        this.outerPolygons = outerPolygons;
        this.innerPolygons = innerPolygons;
        boundingBox = Shape.getBoundingBoxes(outerPolygons);
    }

    public boolean contains(Boundary other) {
        return boundingBox.intersects(other.boundingBox)
                && Polygon.allMatch(other.outerPolygons, outerPolygons, Polygon::isContainedBy)
                && Polygon.isNotContainedBy(other.outerPolygons, innerPolygons);
    }

    public boolean intersects(Boundary other) {
        return boundingBox.intersects(other.boundingBox)
                && Polygon.allMatch(other.outerPolygons, outerPolygons, Polygon::intersectedBy)
                && Polygon.isNotContainedBy(other.outerPolygons, innerPolygons);
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }
}
