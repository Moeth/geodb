package de.neofonie.osm.reader.pipeline;

public class Node extends AbstractEntity<org.openstreetmap.osmosis.core.domain.v0_6.Node> {

    private final double longitude;
    private final double latitude;

    public Node(org.openstreetmap.osmosis.core.domain.v0_6.Node value) {
        super(value);
        longitude = value.getLongitude();
        latitude = value.getLatitude();
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
