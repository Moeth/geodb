package de.neofonie.osm.util;

import com.google.common.collect.ImmutableSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TagUtil {

    private static final Logger log = LoggerFactory.getLogger(TagUtil.class);
    public static final Set<String> IGNORE = ImmutableSet.of("created_by", "source");

    public static Optional<String> getTagValue(final Collection<Tag> tags, final String name) {
        return tags.stream().filter(i -> i.getKey().equals(name)).map(Tag::getValue).findAny();
    }

    public static boolean containsAny(final Collection<Tag> tags, final Collection<String> keys) {
        return tags.stream().map(Tag::getKey).anyMatch(keys::contains);
    }

    public static boolean containsAll(final Collection<Tag> tags, final Collection<String> keys) {
        return tags.stream().map(Tag::getKey).collect(Collectors.toSet()).containsAll(keys);
    }

    public static String toString(final Collection<Tag> tags) {
        return tags.stream()
                .filter(t -> !IGNORE.contains(t.getKey()))
                .map(t -> t.getKey() + "=" + t.getValue())
                .collect(Collectors.joining(","));
    }
}
