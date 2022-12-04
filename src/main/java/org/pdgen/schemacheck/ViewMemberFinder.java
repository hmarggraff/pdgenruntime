// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.schemacheck;

import org.pdgen.data.*;
import org.pdgen.data.view.*;


import org.pdgen.env.Env;

import java.util.HashSet;
import java.util.Set;

public class ViewMemberFinder {
    /**
     * checks that template local formulas are marked as not defined in a view
     */

    public static void markAllFieldsInViews() {
        final Repository repos = Env.instance().repo();
        final HashSet<Object> seen = new HashSet<Object>();

        for (CastAccess a : repos.userRoots.getData()) {
            markAccesses(a, seen);
        }
        // class and collection views
        for (ClassProjection v : Env.instance().repo().classProjections) {
            markInTypes(v, seen);
        }
        for (CollectionProjection v : Env.instance().repo().collectionProjections) {
            markInTypes(v.getElementType(), seen);
        }
    }

    static void markAccesses(JoriaAccess a, final Set<Object> seen) {
        if (!(a instanceof NameableAccess) || seen.contains(a))
            return;
        seen.add(a);
        NameableAccess na = (NameableAccess) a;
        na.setDefinedInView(true);
        final JoriaType type = a.getType();
        if (type.isClass()) {
            markInTypes((JoriaClass) type, seen);
        } else if (type.isCollection()) {
            JoriaCollection jc = (JoriaCollection) type;
            markInTypes(jc.getElementType(), seen);
        }
        if (na instanceof IndirectAccess) {
            IndirectAccess ia = (IndirectAccess) na;
            markAccesses(ia.getBaseAccess(), seen);
        }
    }

    static void markInTypes(final JoriaClass tClass, Set<Object> seen) {
        if (seen.contains(tClass))
            return;
        seen.add(tClass);
        final JoriaAccess[] members = tClass.getMembers();
        for (JoriaAccess member : members) {
            markAccesses(member, seen);
        }
    }


}