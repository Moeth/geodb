package de.neofonie.osm.reader.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class TypeStore<T extends Identifiable> {

    private static final Logger log = LoggerFactory.getLogger(TypeStore.class);
    private final Map<Long, T> map = new HashMap<>();
    private final Set<Long> missing = new HashSet<>();
    private boolean somethingNewMissing = false;

    void addToMap(T entity) {
        missing.remove(entity.getId());
        final T old = map.put(entity.getId(), entity);
        if (old != null && !old.equals(entity)) {
            throw new IllegalArgumentException(String.format("Duplicate key %d", entity.getId()));
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

    boolean isMissing(T entity) {
        return missing.contains(entity.getId());
    }

    public Map<Long, T> getMap() {
        return Collections.unmodifiableMap(map);
    }

    public T get(long id) {
        return map.get(id);
    }
}
