package de.neofonie.osm.reader;

import crosby.binary.osmosis.OsmosisReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PbfReader {

    private static final Logger log = LoggerFactory.getLogger(PbfReader.class);

    public static Stream<Entity> read(File file) throws IOException {
//        StreamSupport.stream(iterable.spliterator(), false);

        log.info(String.format("read %s", file.getAbsolutePath()));
        final long start = System.currentTimeMillis();

//        Sink sinkImplementation = new MySink(consumer);
        try (BufferedInputStream input = openInputStream(file)) {
            final RunnableSource reader = new OsmosisReader(input);
            reader.setSink(sinkImplementation);
            reader.run();
        }

        final long duration = (System.currentTimeMillis() - start) / 1000;
        log.info(String.format("read %s finished in %ds", file.getAbsolutePath(), duration));
    }

    public static void read(File file, Consumer<Entity> consumer) throws IOException {
        log.info(String.format("read %s", file.getAbsolutePath()));
        final long start = System.currentTimeMillis();

        Sink sinkImplementation = new MySink(consumer);
        try (BufferedInputStream input = openInputStream(file)) {
            final RunnableSource reader = new OsmosisReader(input);
            reader.setSink(sinkImplementation);
            reader.run();
        }

        final long duration = (System.currentTimeMillis() - start) / 1000;
        log.info(String.format("read %s finished in %ds", file.getAbsolutePath(), duration));
    }

    private static BufferedInputStream openInputStream(final File file) throws FileNotFoundException {
        if (!file.getName().endsWith(".pbf")) {
            throw new IllegalArgumentException("Only pbf supported");
        }

        return new BufferedInputStream(new FileInputStream(file));
    }

    @RequiredArgsConstructor
    private static class MySink implements Sink {

        @NonNull
        private final Consumer<Entity> consumer;
        private long i = 0;

        @Override
        public void process(EntityContainer entityContainer) {
            i++;

            Entity entity = entityContainer.getEntity();
//            String name = TagUtil.getName(entity);
//            log.info("entity " + name + "\n" + TagUtil.getTags(entity.getTags()) + " " + entity);
            if (i % 100000000 == 0) {
                log.info(String.format("read %,d entries (%s)", i, entity.getClass().getName()));
            }
            consumer.accept(entityContainer.getEntity());
        }

        @Override
        public void release() {
        }

        @Override
        public void initialize(Map<String, Object> stringObjectMap) {

        }

        @Override
        public void complete() {

        }
    }
}
