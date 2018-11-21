package de.neofonie.osm.reader.consumer;

import de.neofonie.osm.util.TagUtil;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LoggingConsumer implements Consumer<Entity> {

    private static final Logger log = LoggerFactory.getLogger(LoggingConsumer.class);

    @Override
    public void accept(final Entity entity) {
        Collection<Tag> tags = entity.getTags();
        if (tags.stream().anyMatch(t -> t.getValue().contains("Alexander"))) {
            String tagss = TagUtil.toString(tags);
//            if (!tagss.isEmpty()) {
//                log.info(entity.toString() + " " + tagss);
//            }
            if (entity instanceof Way) {
                Way way = (Way) entity;
                log.info(entity.toString() + " " + tagss + " " + way.getWayNodes().stream().map(w -> w.toString()).collect(Collectors.joining(",")));
            } else {
                log.info(entity.toString() + " " + tagss);
            }
        }
    }
}
