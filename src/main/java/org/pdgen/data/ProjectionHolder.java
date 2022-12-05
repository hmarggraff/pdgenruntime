// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.ClassProjection;
import org.pdgen.data.view.CollectionProjection;
import org.pdgen.env.Env;

import java.util.Objects;


public class ProjectionHolder implements Named {
    protected JoriaClass base;
    protected SortedNamedVector<ClassProjection> projections;
    protected SortedNamedVector<CollectionProjection> collections; // collection views that have this class as element type

    public ProjectionHolder(JoriaClass p) {
        base = p;
    }

    public SortedNamedVector<CollectionProjection> getCollections() {
        return collections;
    }

    public SortedNamedVector<ClassProjection> getProjections() {
        return projections;
    }

    public void addClassView(ClassProjection p) {
        if (projections == null) {
            projections = new SortedNamedVector<>();
        }
        projections.add(p);
        Env.repoChanged();
    }

    public void addCollectionView(CollectionProjection p) {
        if (collections == null) {
            collections = new SortedNamedVector<>();
        }
        collections.add(p);
        Env.repoChanged();
    }

    public boolean equals(Object o) {
        if (o instanceof JoriaClass)
            return base.getName().equals(((JoriaClass) o).getName());
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(base.getName());
    }

    public JoriaClass getBase() {
        return base;
    }

    public String getName() {
        return base.getName();
    }


    public void remove(ClassProjection p) {
        if (projections != null)
            projections.remove(p);
    }


    public String toString() {
        return base.getName();
    }
}
