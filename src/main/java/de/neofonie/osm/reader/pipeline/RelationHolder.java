package de.neofonie.osm.reader.pipeline;

import de.neofonie.osm.reader.PbfReader;
import de.neofonie.osm.reader.data.Administration;
import de.neofonie.osm.reader.data.Street;
import de.neofonie.osm.reader.vector.Boundary;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RelationHolder implements Consumer<Entity> {

    private static final Logger log = LoggerFactory.getLogger(RelationHolder.class);
    private final TypeStore2<NodeData> nodes = new TypeStore2<>();
    private final TypeStore2<Way> ways = new TypeStore2<>();
    private final TypeStore<Relation> relations = new TypeStore<>();

    private final Predicate<AbstractEntity> predicate;
    private final Predicate<Way> wayPredicate;

    public static RelationHolder readRelationHolder(File file, Predicate<AbstractEntity> predicate, Predicate<Way> wayPredicate) throws IOException {
        RelationHolder relationHolder = new RelationHolder(predicate, wayPredicate);
        relationHolder.read(file);
        return relationHolder;
    }

    private void read(File file) throws IOException {
        do {
            relations.resetSomethingNewMissing();
            ways.resetSomethingNewMissing();
            nodes.resetSomethingNewMissing();
            PbfReader.read(file, this);
        } while (relations.isSomethingNewMissing() || ways.isSomethingNewMissing() || nodes.isSomethingNewMissing());
    }

    private RelationHolder(Predicate<AbstractEntity> predicate, final Predicate<Way> wayPredicate) {
        this.predicate = predicate;
        this.wayPredicate = wayPredicate;
    }

    @Override
    public void accept(final Entity entity) {
//        log.info(entity.toString() + " " + entity.getTags().stream().map(t -> t.toString()).collect(Collectors.joining(",")));
        if (entity instanceof Node) {
            handleNode((Node) entity);
        } else if (entity instanceof org.openstreetmap.osmosis.core.domain.v0_6.Way) {
            handleWay((org.openstreetmap.osmosis.core.domain.v0_6.Way) entity);
        } else if (entity instanceof org.openstreetmap.osmosis.core.domain.v0_6.Relation) {
            handleRelation((org.openstreetmap.osmosis.core.domain.v0_6.Relation) entity);
        } else if (entity instanceof Bound) {
            //            handleBound((Bound) entity);
        } else {
            throw new IllegalArgumentException("Unknown type " + entity);
        }
    }

    private void handleNode(Node node) {
        NodeData n = new NodeData(node);
        if (nodes.isMissing(node.getId())) {
            nodes.addToMap(node.getId(), n);
        }
    }

    private void handleWay(org.openstreetmap.osmosis.core.domain.v0_6.Way way) {
        Way w = new Way(way, this);
        if (wayPredicate.test(w) || ways.isMissing(w.getId())) {
            ways.addToMap(w.getId(), w);
            for (WayNode wayNode : way.getWayNodes()) {
                nodes.addMissing(wayNode.getNodeId());
            }
        }
    }

    private void handleRelation(org.openstreetmap.osmosis.core.domain.v0_6.Relation relation) {
        Relation r = new Relation(relation, this);
        if (predicate.test(r) || relations.isMissing(r)) {
            relations.addToMap(r);
            for (RelationMember relationMember : relation.getMembers()) {
                handleRelationMember(relationMember);
            }
        }
    }

    private void handleRelationMember(RelationMember relationMember) {
        final EntityType memberType = relationMember.getMemberType();
        switch (memberType) {
            case Bound:
                break;
            case Node:
                nodes.addMissing(relationMember.getMemberId());
                break;
            case Relation:
//                log.info(String.format("%s(%d) - %s (%s)", TagUtil.getName(relation), relation.getId(),
//                        relationMember.getMemberId(), relationMember.getMemberRole()));
                relations.addMissing(relationMember.getMemberId());
                break;
            case Way:
                ways.addMissing(relationMember.getMemberId());
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    Map<Long, Relation> getRelations() {
        return relations.getMap();
    }

    public NodeData getNode(long id) {
        return nodes.get(id);
    }

    public AbstractEntity<?> getEntity(RelationMember relationMember) {
        final EntityType memberType = relationMember.getMemberType();
        final long memberId = relationMember.getMemberId();
        switch (memberType) {
            case Way:
                return ways.get(memberId);
            case Relation:
                return relations.get(memberId);
            default:
                throw new IllegalArgumentException("Unknown type " + memberType);
        }
    }

    public List<Administration> createBoundaries() {
        final List<Relation> relations = this.relations.getMap().values()
                .stream()
                .filter(Relation::isAdministrative)
//                .filter(r -> r.isBoundary())
//                .map(r -> r.createBoundary())
                .collect(Collectors.toList());

        List<Administration> result = new ArrayList<>();
        for (Relation relation : relations) {
            fillBoundary(result, relation);
        }
        log.info(String.format("%d boundarys created", result.size()));
        final List<Administration> root = Administration.createHierarchy(result);
        log.info(String.format("%d hierarchy created", root.size()));
        fillStreets(result);
        return root;
    }

    private void fillStreets(List<Administration> result) {
        final List<Street> streets = createStreets();
        Collections.sort(result, Administration.ADMINISTRATION_LEVEL_COMPARATOR.reversed());
        int i = 0;
        for (Street street : streets) {
            for (Administration administration : result) {
                if (administration.getChilds().isEmpty()) {
                    i++;
                    if (i % 1000 == 0) {
                        log.info(String.format("Calculate %d", i));
                    }
                    final boolean contains = administration.getBoundary().intersects(street.getBoundary());
                    if (contains) {
                        administration.addStreet(street);
                    }
                }
            }
        }
    }

    private void fillBoundary(List<Administration> result, Relation relation) {
        if (!relation.isBoundary()) {
            return;
        }
        try {
            final Boundary boundary = Boundary.create(relation);
            if (boundary != null) {
//                log.info(String.format("Boundary created for %s", relation));
                result.add(new Administration(boundary, relation));
            }
        } catch (IllegalArgumentException e) {
            log.warn(String.format("Boundary creation for %s failed - %s", relation, e.getMessage()));
        }
    }

    private List<Street> createStreets() {
        final List<Street> streets = ways.getMap().values()
                .stream()
                .filter(r -> r.isStreet())
//                .filter(r -> r.isBoundary())
                .map(r -> Street.create(r))
                .distinct()
                .collect(Collectors.toList());
//        for (Street street : streets) {
//            log.info("" + street.getTagString());
//        }
        return streets;
    }

    @Override
    public String toString() {
        return "RelationHolder{" +
                "nodes=" + nodes +
                ", ways=" + ways +
                ", relations=" + relations +
                '}';
    }
}
