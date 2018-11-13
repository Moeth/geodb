package de.neofonie.osm.reader.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Relation extends NamedEntity<org.openstreetmap.osmosis.core.domain.v0_6.Relation> {

    private static final Logger log = LoggerFactory.getLogger(Relation.class);
    private final List<RelationMember> members = new ArrayList<>();
    private static final Set<Long> IGNORE = new HashSet<>(Arrays.asList(90124L));

    public Relation(org.openstreetmap.osmosis.core.domain.v0_6.Relation value, RelationHolder relationHolder) {
        super(value);
        for (org.openstreetmap.osmosis.core.domain.v0_6.RelationMember relationMember : value.getMembers()) {
            members.add(new RelationMember(relationMember, relationHolder));
        }
    }

    public List<AbstractEntity<?>> getMembers(String role) {
        return members
                .stream()
                .filter(m -> m.getMemberRole().equals(role))
                .map(RelationMember::getEntity)
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    public List<AbstractEntity<?>> getMembers() {
        return members
                .stream()
                .map(RelationMember::getEntity)
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    public boolean isBoundary() {
        return !IGNORE.contains(getId());
    }

    @Override
    public String toString() {
        return String.format("Relation{%s}", getDescription());
    }

    public boolean isAdministrative() {
        final String boundary = getTag("boundary");
        if (!"administrative".equals(boundary)) {
            return false;
        }

        return getAdminLevel() < Integer.MAX_VALUE;
    }

    public int getAdminLevel() {
        final String adminLevelString = getTag("admin_level");
        if (adminLevelString == null) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(adminLevelString);
        } catch (NumberFormatException e) {
            log.warn("NumberFormatException: " + e.getMessage() + " - " + getTagString());
            return Integer.MAX_VALUE;
        }
    }
}
