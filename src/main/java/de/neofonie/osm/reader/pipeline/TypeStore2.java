package de.neofonie.osm.reader.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class TypeStore2<T> {

    private static final Logger log = LoggerFactory.getLogger(TypeStore2.class);
    private final Map<Long, T> map = new HashMap<>();
    private final Set<Long> missing = new HashSet<>();
    private boolean somethingNewMissing = false;

    void addToMap(long id, T entity) {
        missing.remove(id);
        final T old = map.put(id, entity);
        if (old != null && !old.equals(entity)) {
            throw new IllegalArgumentException(String.format("Duplicate key %d", id));
        }
    }

    boolean isSomethingNewMissing() {
        return somethingNewMissing;
    }

    void resetSomethingNewMissing() {
        somethingNewMissing = false;
    }

    void addMissing(long id) {
        if (map.containsKey(id)) {
            return;
        }

        if (missing.add(id)) {
            somethingNewMissing = true;
        }
    }

    boolean isMissing(long id) {
        return missing.contains(id);
    }

    public Map<Long, T> getMap() {
        return Collections.unmodifiableMap(map);
    }

    public T get(long id) {
        return map.get(id);
    }
}
