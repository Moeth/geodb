package de.neofonie.osm.reader.pipeline;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

@Data
@EqualsAndHashCode(of = "id")
public class NodeData implements Identifiable {

    private final long id;
    private final double longitude;
    private final double latitude;

    public NodeData(Node value) {
        id = value.getId();
        longitude = value.getLongitude();
        latitude = value.getLatitude();
    }
}
