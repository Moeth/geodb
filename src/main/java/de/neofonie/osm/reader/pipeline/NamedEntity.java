package de.neofonie.osm.reader.pipeline;

import com.google.common.base.Preconditions;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NamedEntity<T extends Entity> extends AbstractEntity<T> {

    private static final Logger log = LoggerFactory.getLogger(NamedEntity.class);
    private final Collection<Tag> tags;
    private static final Collection<String> ALLOWED_TAGS = new HashSet<>(Arrays.asList("de:name", "name", "boundary", "admin_level", "type", "highway"));

    NamedEntity(T value) {
        super(value);
        tags = value.getTags().stream().filter(t -> ALLOWED_TAGS.contains(t.getKey())).collect(Collectors.toList());
    }

    public List<String> getTags(String name) {
        final List<String> list = getTagStream(name)
                .map(Tag::getValue).collect(Collectors.toList());
        return list;
    }

    public String getName() {
        final String name = getTag("de:name");
        if (name != null) {
            return name;
        }

        return getTag("name");
    }

    public String getDescription() {
        return String.format("%s(%d)", getName(), getId());
    }

    public String getTagString() {
        return tags.stream()
//                .filter(t -> !t.getKey().startsWith("name:") || t.getKey().equals("long_name:"))
//                         .filter(t -> t.getKey().startsWith("addr:"))
                .map(t -> t.toString())
                .collect(Collectors.joining(","));
    }

    public String getTag(String name) {
        Optional<Tag> first = getTagStream(name).findFirst();
        if (first.isPresent()) {
            return first.get().getValue();
        }
        return null;
    }

    private Stream<Tag> getTagStream(String name) {
        Preconditions.checkArgument(ALLOWED_TAGS.contains(name), String.format("%s is not saved", name));
        return tags
                .stream()
                .filter(t -> t.getKey().equals(name));
    }
}
