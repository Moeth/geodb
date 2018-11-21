package de.neofonie.osm.reader;

import de.neofonie.osm.reader.data.Administration;
import de.neofonie.osm.reader.data.Street;
import de.neofonie.osm.reader.pipeline.AbstractEntity;
import de.neofonie.osm.reader.pipeline.Relation;
import de.neofonie.osm.reader.pipeline.RelationHolder;
import de.neofonie.osm.reader.pipeline.Way;
import de.neofonie.osm.reader.street.AddressBean;
import de.neofonie.osm.reader.street.AddressExtractor;
import org.apache.commons.collections4.CollectionUtils;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class OsmReaderService {

    private static final Logger log = LoggerFactory.getLogger(OsmReaderService.class);

    public void read(File file) throws IOException {

        final Predicate<AbstractEntity> predicate = new Predicate<AbstractEntity>() {
            @Override
            public boolean test(AbstractEntity entity) {
                if (entity.getId() == 1111111L) {
                    return false;
                }

                return entity instanceof Relation && ((Relation) entity).isAdministrative();
//                if ("postal_code".equals(entity.getTag("boundary"))) {
//                    log.info("postal_code " + entity);
//                }
//                return entity.isStreet();
            }
        };
        Consumer<EntityContainer> entityContainerConsumer = entityContainer -> {
            final Entity entity = entityContainer.getEntity();
            log.info(entity.toString() + " " + entity.getTags().stream().map(t -> t.toString()).collect(Collectors.joining(",")));
        };

//        PbfReader.read(file, new LoggingConsumer());

        final Predicate<Way> wayPre = way -> {
            final List<String> highway = way.getTags("highway");
            return CollectionUtils.containsAny(highway, AbstractEntity.HIGHWAY_RELEVANT)
                    && way.getName() != null;
        };
        final RelationHolder relationHolder = RelationHolder.readRelationHolder(file, predicate, wayPre);
        final List<Administration> roots = relationHolder.createBoundaries();
        writeBoundaryHierarchy(roots);
    }

    public void readStreets(final File file) throws IOException {
        PbfReader.read(file, new AddressExtractor(new Consumer<AddressBean>() {
            int i = 0;

            @Override
            public void accept(final AddressBean addressBean) {
                int f = i++;
                log.info(f + " " + addressBean.toString());
            }
        }));
    }

    private void writeBoundaryHierarchy(List<Administration> roots) throws IOException {
        FileWriter fileWriter = new FileWriter("out.txt");
        try {
            for (Iterator<Administration> iterator = Administration.getAllIterator(roots); iterator.hasNext(); ) {
                Administration boundary = iterator.next();
                final String collect = boundary
                        .getAncestorsAndSelf()
                        .stream()
                        .map(b -> {
                            final Relation relation = b.getRelation();
                            return String.format("%s(%d,%d)", relation.getName(), relation.getId(), relation.getAdminLevel());
                        })
                        .collect(Collectors.joining("\t"));
                fileWriter.write(collect + "\n");
                if (!boundary.getStreets().isEmpty()) {
//                    final String streetnames = boundary.getStreets()
//                            .stream()
//                            .map(b -> String.format("%s(%d)", b.getWay().getName(), b.getWay().getId()))
//                            .collect(Collectors.joining("\t"));

//                    Collector<Way, ?, Map<String, Integer>> objectMapCollector = (Collector<Way, ?, Map<String, Integer>>) tMapCollector;
                    String streetnames = boundary.getStreets().stream()
                            .map(Street::getWay)
                            .collect(Collectors.groupingBy(b -> b.getName()))
                            .entrySet()
                            .stream()
                            .map(s -> {

                                String collect2 = s.getValue().stream()
                                        .map(AbstractEntity::getId)
                                        .map(a -> Long.toString(a))
                                        .collect(Collectors.joining(","));
                                return String.format("%s(%s)", s.getKey(), collect2);
                            })
                            .collect(Collectors.joining("\t"));
                    fileWriter.write(streetnames + "\n");
                }
//                log.info("" + collect);
            }
        } finally {
            fileWriter.close();
        }
        log.info("written to out.txt");

//        Gson gson = new Gson();
////        gson.newJsonWriter(new FileWriter("asdfas.json")).
//        try (Writer writer = new BufferedWriter(new FileWriter("out.json"))) {
//            gson.toJson(roots, writer);
//        }
//        log.info("written to out.json");
    }

    private void writeBoundaryHierarchy(List<Administration> roots, List<Way> streets) throws IOException {
        FileWriter fileWriter = new FileWriter("out.txt");
        try {
            for (Iterator<Administration> iterator = Administration.getAllIterator(roots); iterator.hasNext(); ) {
                Administration boundary = iterator.next();
                final String collect = boundary
                        .getAncestorsAndSelf()
                        .stream()
                        .map(b -> {
                            final Relation relation = b.getRelation();
                            return String.format("%s(%d,%d)", relation.getName(), relation.getId(), relation.getAdminLevel());
                        })
                        .collect(Collectors.joining("\t"));
//                boundary.getBoundary().getIntersections(streets);
                fileWriter.write(collect + "\n");
//                log.info("" + collect);
            }
        } finally {
            fileWriter.close();
        }
        log.info("written to out.txt");
    }
}