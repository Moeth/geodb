package de.neofonie.osm.reader.consumer;

import com.google.common.collect.ImmutableSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LoggingConsumer implements Consumer<Entity> {

    private static final Logger log = LoggerFactory.getLogger(LoggingConsumer.class);
    private static final Set<String> IGNORE = ImmutableSet.of("created_by", "source");

    @Override
    public void accept(final Entity entity) {
        Collection<Tag> tags = entity.getTags();
        if (tags.stream().anyMatch(t -> t.getValue().contains("Alexander"))) {
            String tagss = tags.stream()
                    .filter(t -> !IGNORE.contains(t.getKey()))
                    .map(t -> t.getKey() + "=" + t.getValue())
                    .collect(Collectors.joining(","));
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
