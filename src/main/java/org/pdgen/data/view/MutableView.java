// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;

import java.util.Map;
import java.util.Set;

/**
 * this is the base interface for ClassProjection and CollectionProjection
 * It adds mutability to the views
 * if it is a collection the methods are delegated to the element class of the collection
 * Note that MutableView neither implements JoriaClass nor JoriaCollection directly
 * in order to keep those interfaces separated
 */
public interface MutableView extends JoriaType, VariableProvider, Nameable, Rebindable {
    /*
     * Add a Fieldaccess to this ClassProjection (ClassView)
     * Child is added at the end.
     */
    void addChild(JoriaAccess f);

    JoriaAccess findMember(String f);

    void removeChild(JoriaAccess f);

    void replaceChild(JoriaAccess f, JoriaAccess fNew);

    JoriaType getOriginalType();

    void setBase(JoriaClass c);

    boolean fixAccess();

    MutableView dup(Map<Object, Object> alreadyCopiedViews);

    void collectUsedViews(Set<MutableView> s);

    void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews);

    boolean hasName();

    void sortMembers();
}
