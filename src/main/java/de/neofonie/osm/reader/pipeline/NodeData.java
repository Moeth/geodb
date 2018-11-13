package de.neofonie.osm.reader.pipeline;

import lombok.Data;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

@Data
public class NodeData {

    private final double longitude;
    private final double latitude;

    public NodeData(Node value) {
        longitude = value.getLongitude();
        latitude = value.getLatitude();
    }
}
