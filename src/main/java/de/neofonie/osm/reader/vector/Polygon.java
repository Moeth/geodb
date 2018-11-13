package de.neofonie.osm.reader.vector;

import com.google.common.base.Preconditions;
import de.neofonie.osm.reader.pipeline.AbstractEntity;
import de.neofonie.osm.reader.pipeline.NodeData;
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

    private Polygon(List<NodeData> polygonLine) {
        area = createArea(polygonLine);
    }

    public static Polygon createPolygon(Way way) {
        return new Polygon(way.getWayNodeData());
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

    private static Area createArea(List<NodeData> polygonLine) {
        Path2D.Double polygonPath = new Path2D.Double();
        NodeData previous = null;
        for (final NodeData nodeData : polygonLine) {
            if (previous == null) {
                polygonPath.moveTo(nodeData.getLongitude(), nodeData.getLatitude());
            } else if (!previous.equals(nodeData)) {
                polygonPath.lineTo(nodeData.getLongitude(), nodeData.getLatitude());
            }
            previous = nodeData;
        }
        return new Area(polygonPath);
    }

    private static Polygon createPolygonLine(final List<Way> ways) {

        final List<NodeData> result = new ArrayList<>();
        addToResult(ways, result, ways.get(0));
        final NodeData first = result.get(0);
        while (!ways.isEmpty()) {
            final NodeData last = result.get(result.size() - 1);

            if (first.equals(last)) {
                //Polygon found - there exist another one
                return new Polygon(result);
            }
            Way current = getFollowingWay(last, ways);
            addToResult(ways, result, current);
        }
        final NodeData last = result.get(result.size() - 1);
        Preconditions.checkArgument(first.equals(last));
        return new Polygon(result);
    }

    private static void addToResult(List<Way> ways, List<NodeData> result, Way add) {
        ways.remove(add);
        final NodeData lastWayNodeData = add.getLastWayNode();
        final NodeData firstWayNodeData = add.getFirstWayNode();
        if (result.isEmpty()) {
            result.addAll(add.getWayNodeData());
            return;
        }
        final NodeData lastResult = result.get(result.size() - 1);
        if (lastResult.equals(firstWayNodeData)) {
            result.addAll(add.getWayNodeData());
        } else if (lastResult.equals(lastWayNodeData)) {
            final List<NodeData> wayNodeData = new ArrayList<>(add.getWayNodeData());
            Collections.reverse(wayNodeData);
            result.addAll(wayNodeData);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static Way getFollowingWay(final NodeData lastWayNodeData, List<Way> ways) {
        Preconditions.checkNotNull(lastWayNodeData);
        return ways.stream()
                .filter(way -> lastWayNodeData.equals(way.getFirstWayNode()) || lastWayNodeData.equals(way.getLastWayNode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not closed polygon"));
    }

    private static String toString(String message, List<NodeData> result, List<Way> ways) {
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

    private static String toStringNodes(List<NodeData> nodeData) {
        return nodeData.get(0) + "..." + nodeData.get(nodeData.size() - 1);
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
