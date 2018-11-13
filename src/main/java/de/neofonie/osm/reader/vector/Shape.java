package de.neofonie.osm.reader.vector;

import java.awt.geom.Rectangle2D;
import java.util.Collection;

public interface Shape {

    public Rectangle2D getBoundingBox();

    public static Rectangle2D getBoundingBoxes(Collection<? extends Shape> shapes) {

        Rectangle2D result = null;
        for (Shape shape : shapes) {
            final Rectangle2D shapeBoundingBox = shape.getBoundingBox();
            if (result == null) {
                result = (Rectangle2D) shapeBoundingBox.clone();
            } else {
                result = result.createUnion(shapeBoundingBox);
            }
        }
        return result;
    }
}
