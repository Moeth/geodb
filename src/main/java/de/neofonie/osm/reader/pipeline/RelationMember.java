package de.neofonie.osm.reader.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RelationMember {

    private static final Logger log = LoggerFactory.getLogger(RelationMember.class);
    private final org.openstreetmap.osmosis.core.domain.v0_6.RelationMember relationMember;
    private final RelationHolder relationHolder;

    RelationMember(org.openstreetmap.osmosis.core.domain.v0_6.RelationMember relationMember, RelationHolder relationHolder) {
        this.relationMember = relationMember;
        this.relationHolder = relationHolder;
    }

    public AbstractEntity<?> getEntity() {
        return relationHolder.getEntity(relationMember);
    }

    public String getMemberRole() {
        return relationMember.getMemberRole();
    }
}
