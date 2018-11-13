package de.neofonie.osm.reader.vector;

import com.google.common.base.Preconditions;
import de.neofonie.osm.reader.pipeline.AbstractEntity;
import de.neofonie.osm.reader.pipeline.Node;
import de.neofonie.osm.reader.pipeline.Way;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Polygon implements Shape {

    private final Area area;

    private Polygon(List<Node> polygonLine) {
        area = createArea(polygonLine);
    }

    public static Polygon createPolygon(Way way) {
        return new Polygon(way.getWayNodes());
    }

    public static List<Polygon> createClosedPolygons(List<AbstractEntity<?>> entities) {
        List<Polygon> result = new ArrayList<>();
        if (entities.isEmpty()) {
            return result;
        }
        final List<Way> ways = Way.getWays(entities);
        Preconditions.checkArgument(!ways.isEmpty());
        while (!ways.isEmpty()) {
            final Polygon polygon = createPolygonLine(ways);
            result.add(polygon);
        }
        return result;
    }

    private static Area createArea(List<Node> polygonLine) {
        Path2D.Double polygonPath = new Path2D.Double();
        Node previous = null;
        for (final Node node : polygonLine) {
            if (previous == null) {
                polygonPath.moveTo(node.getLongitude(), node.getLatitude());
            } else if (!previous.equals(node)) {
                polygonPath.lineTo(node.getLongitude(), node.getLatitude());
            }
            previous = node;
        }
        return new Area(polygonPath);
    }

    private static Polygon createPolygonLine(final List<Way> ways) {

        final List<Node> result = new ArrayList<>();
        addToResult(ways, result, ways.get(0));
        final Node first = result.get(0);
        while (!ways.isEmpty()) {
            final Node last = result.get(result.size() - 1);

            if (first.equals(last)) {
                //Polygon found - there exist another one
                return new Polygon(result);
            }
            Way current = getFollowingWay(last, ways);
            addToResult(ways, result, current);
        }
        final Node last = result.get(result.size() - 1);
        Preconditions.checkArgument(first.equals(last));
        return new Polygon(result);
    }

    private static void addToResult(List<Way> ways, List<Node> result, Way add) {
        ways.remove(add);
        final Node lastWayNode = add.getLastWayNode();
        final Node firstWayNode = add.getFirstWayNode();
        if (result.isEmpty()) {
            result.addAll(add.getWayNodes());
            return;
        }
        final Node lastResult = result.get(result.size() - 1);
        if (lastResult.equals(firstWayNode)) {
            result.addAll(add.getWayNodes());
        } else if (lastResult.equals(lastWayNode)) {
            final List<Node> wayNodes = new ArrayList<>(add.getWayNodes());
            Collections.reverse(wayNodes);
            result.addAll(wayNodes);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static Way getFollowingWay(final Node lastWayNode, List<Way> ways) {
        Preconditions.checkNotNull(lastWayNode);
        return ways.stream()
                .filter(way -> lastWayNode.equals(way.getFirstWayNode()) || lastWayNode.equals(way.getLastWayNode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not closed polygon"));
    }

    private static String toString(String message, List<Node> result, List<Way> ways) {
        return String.format("%s (\ncurrent:\n" +
                "\t%s\nremaining:\n" +
                "\t%s)", message, toStringNodes(result), toStringWay(ways));
    }

    private static String toStringWay(List<Way> ordered) {
        return ordered
                .stream()
                .map(m -> m.getFirstWayNode() + "..." + m.getLastWayNode())
                .collect(Collectors.joining(",\n\t"));
    }

    private static String toStringNodes(List<Node> nodes) {
        return nodes.get(0) + "..." + nodes.get(nodes.size() - 1);
    }

    public boolean isNodeWithinArea(org.openstreetmap.osmosis.core.domain.v0_6.Node node) {
        return area.contains(node.getLongitude(), node.getLatitude());
    }

    public boolean intersects(Polygon polygon) {
        if (!getBoundingBox().intersects(polygon.getBoundingBox())) {
            return false;
        }

        Area clone = (Area) area.clone();
        clone.intersect(polygon.area);
        return !clone.isEmpty();
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return area.getBounds2D();
    }

    public boolean contains(Polygon polygon) {
        if (!getBoundingBox().contains(polygon.getBoundingBox())) {
            return false;
        }

        Area clone = (Area) area.clone();
        clone.intersect(polygon.area);
        return !clone.isEmpty();
    }

    public boolean isContainedBy(Collection<Polygon> polygons) {
        return polygons.stream().anyMatch(polygon -> polygon.contains(this));
    }

    public boolean intersectedBy(Collection<Polygon> polygons) {
        return polygons.stream().anyMatch(polygon -> polygon.intersects(this));
    }

    public static boolean isNotContainedBy(Collection<Polygon> o1, Collection<Polygon> o2) {
        return o1.stream().noneMatch(polygon -> polygon.isContainedBy(o2));
    }

    public static boolean allMatch(final Collection<Polygon> p1, final Collection<Polygon> p2, BiFunction<Polygon, Collection<Polygon>, Boolean> function) {
        return p1.stream().allMatch(polygon -> function.apply(polygon, p2));
    }
}
