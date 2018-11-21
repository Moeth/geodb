package de.neofonie.osm.reader.data;

import com.google.common.base.Preconditions;
import de.neofonie.osm.reader.pipeline.Relation;
import de.neofonie.osm.reader.vector.Boundary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Administration {

    private static final Logger log = LoggerFactory.getLogger(Administration.class);
    public static final Comparator<Administration> ADMINISTRATION_LEVEL_COMPARATOR = Comparator.comparingInt(Administration::getAdminLevel);

    private final Boundary boundary;
    private final Relation relation;
    private Administration parent;
    private final Set<Administration> childs = new HashSet<>();
    private final Set<Street> streets = new HashSet<>();

    public Administration(Boundary boundary, Relation relation) {
        this.boundary = boundary;
        this.relation = relation;
    }

    public static List<Administration> createHierarchy(List<Administration> boundaries) {

        List<Administration> result = new ArrayList<>();
        final List<List<Administration>> hierarchyList = getHierarchyList(boundaries);
//        Map<Boundary, Boundary> hierarchy = new HashMap<>();

        List<Administration> ancestors = new ArrayList<>();
        for (List<Administration> hierarchyLevel : hierarchyList) {
            if (ancestors.isEmpty()) {
                result.addAll(hierarchyLevel);
            } else {
                for (Administration boundary : hierarchyLevel) {
                    Preconditions.checkArgument(!ancestors.contains(boundary));
                    final Administration parent = getParent(ancestors, boundary);
                    if (parent != null) {
                        Preconditions.checkArgument(ancestors.contains(parent));
                        Preconditions.checkArgument(!boundary.equals(parent), boundary + " equals " + parent);
                        boundary.setParent(parent);
                    } else {
                        log.info(String.format("No parent for %s found", boundary));
                    }
                }
            }
            ancestors.addAll(hierarchyLevel);
        }
        return result;
    }

    private static List<List<Administration>> getHierarchyList(List<Administration> boundaries) {
        Collections.sort(boundaries, ADMINISTRATION_LEVEL_COMPARATOR);

        List<List<Administration>> hierarchyList = new ArrayList<>();
        int i = -1;
        List<Administration> lastList = new ArrayList<>();
        for (Administration boundary : boundaries) {
            if (i != boundary.getAdminLevel()) {
                if (!lastList.isEmpty()) {
                    hierarchyList.add(lastList);
                    lastList = new ArrayList<>();
                }
                i = boundary.getAdminLevel();
            }
            lastList.add(boundary);
        }
        return hierarchyList;
    }

    private static Administration getParent(List<Administration> boundaries, Administration child) {
        for (int j = boundaries.size() - 1; j >= 0; j--) {
            Administration parent = boundaries.get(j);
            if (parent.boundary.contains(child.boundary)) {
                return parent;
            }
        }
        return null;
    }

    public Administration getParent() {
        return parent;
    }

    void setParent(Administration parent) {
        Preconditions.checkArgument(this.parent == null);
        Preconditions.checkArgument(!equals(parent), this + " equals " + parent);
        this.parent = parent;
        parent.childs.add(this);
    }

    public Set<Administration> getChilds() {
        return Collections.unmodifiableSet(childs);
    }

    public static Iterator<Administration> getAllIterator(Collection<Administration> boundaries) {
        return new BoundaryIterator(boundaries);
    }

    public void addStreet(Street street) {
        streets.add(street);
    }

    public Set<Street> getStreets() {
        return Collections.unmodifiableSet(streets);
    }

    private static class BoundaryIterator implements Iterator<Administration> {

        private final LinkedList<Administration> boundaries = new LinkedList<>();

        private BoundaryIterator(Collection<Administration> boundaries) {
            this.boundaries.addAll(boundaries);
        }

        @Override
        public boolean hasNext() {
            return !boundaries.isEmpty();
        }

        @Override
        public Administration next() {
            final Administration first = boundaries.pollFirst();
            if (first == null) {
                throw new NoSuchElementException();
            }
            boundaries.addAll(0, first.getChilds());
            return first;
        }
    }

    public List<Administration> getAncestorsAndSelf() {
        LinkedList<Administration> result = new LinkedList<>();
        Administration current = this;
        while (current != null) {
            result.addFirst(current);
            current = current.parent;
        }
        return result;
    }

    public Relation getRelation() {
        return relation;
    }

    @Override
    public String toString() {
        return "Boundary{" +
                "relation=" + relation +
                '}';
    }

    public int getAdminLevel() {
        return relation.getAdminLevel();
    }

    public Boundary getBoundary() {
        return boundary;
    }
}
