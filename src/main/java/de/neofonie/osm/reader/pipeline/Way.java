package de.neofonie.osm.reader.pipeline;

import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Way extends NamedEntity<org.openstreetmap.osmosis.core.domain.v0_6.Way> {

    private static final Logger log = LoggerFactory.getLogger(Way.class);
    private final List<Node> wayNodes = new ArrayList<>();
    private final List<Long> wayNodesIds = new ArrayList<>();
    private final RelationHolder relationHolder;

    public Way(org.openstreetmap.osmosis.core.domain.v0_6.Way value, RelationHolder relationHolder) {
        super(value);
        this.relationHolder = relationHolder;
        for (WayNode wayNode : value.getWayNodes()) {
            wayNodesIds.add(wayNode.getNodeId());
        }
    }

    public List<Node> getWayNodes() {
        if (wayNodes.isEmpty()) {
            for (Long wayNode : wayNodesIds) {
                final Node node = relationHolder.getNode(wayNode);
                if (node != null) {
                    wayNodes.add(node);
                }
            }
        }
        return Collections.unmodifiableList(wayNodes);
    }

    public static List<Way> getWays(List<AbstractEntity<?>> entities) {
        List<Way> result = new ArrayList<>();
        for (AbstractEntity abstractEntity : entities) {
            if (abstractEntity instanceof Way) {
                result.add((Way) abstractEntity);
            } else if (abstractEntity instanceof Relation) {
                final Relation relation = (Relation) abstractEntity;
                final List<Way> ways = getWays(relation.getMembers());
                result.addAll(ways);
            } else {
                throw new IllegalArgumentException("type " + abstractEntity);
            }
        }
        return result;
    }

    public Node getLastWayNode() {
        final List<Node> wayNodes = getWayNodes();
        return wayNodes.get(wayNodes.size() - 1);
    }

    public Node getFirstWayNode() {
        final List<Node> wayNodes = getWayNodes();
        return wayNodes.get(0);
    }
}
