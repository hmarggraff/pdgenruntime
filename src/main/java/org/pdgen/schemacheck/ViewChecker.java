// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.schemacheck;

import org.pdgen.data.*;
import org.pdgen.data.view.*;


import org.pdgen.env.Env;
import org.pdgen.projection.ComputedField;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * maintains a weak list of all views for housekeeping
 * and routines to find views
 */
public class ViewChecker {
    public static List<WeakReference<JoriaClass>> extentViews = new ArrayList<WeakReference<JoriaClass>>();

    public static void checkAllViews() {
        final Repository repos = Env.instance().repo();
        final HashSet<JoriaClass> seenClasses = new HashSet<JoriaClass>();
        final Set<ClassView> foundViews = new HashSet<ClassView>();

        for (CastAccess a : repos.userRoots.getData()) {
            final JoriaClass tClass = (JoriaClass) a.getType();
            findViews(tClass, seenClasses, foundViews);
        }
        System.out.println("Views loaded " + extentViews.size() + " found = " + foundViews.size());
        /**
         * we must distinguish between formulas, that are created in aview and then used, and local formulas in a template.
         * The second must be duplicated with their template, the first not.
         */
        for (ClassView v : foundViews) {
            for (JoriaAccess m : v.getMembers()) {
                if (m instanceof ComputedField)
                    ((ComputedField) m).setDefinedInView(true);
            }
        }
        // check for named views and collection views, that are independent of a root
        for (ClassProjection v : repos.classProjections) {
            for (JoriaAccess m : v.getMembers()) {
                if (m instanceof ComputedField)
                    ((ComputedField) m).setDefinedInView(true);
            }
        }
        for (CollectionProjection v : repos.collectionProjections) {
            for (JoriaAccess m : v.getElementType().getMembers()) {
                if (m instanceof ComputedField) {
                    final ComputedField cf = (ComputedField) m;
                    cf.setDefinedInView(true);
                }
            }
        }
    }

    private static void findViews(final JoriaClass tClass, Set<JoriaClass> seen, Set<ClassView> foundViews) {
        if (seen.contains(tClass))
            return;
        seen.add(tClass);
        if (tClass instanceof ClassView)
            foundViews.add((ClassView) tClass);
        final JoriaAccess[] members = tClass.getMembers();
        for (JoriaAccess member : members) {
            final JoriaType type = member.getType();
            if (type.isClass()) {
                if (!(type instanceof ClassView))
                    System.out.println("ViewCheck: Non View in View = " + type.getClass().getName());
                findViews((JoriaClass) type, seen, foundViews);
            } else if (type.isCollection()) {
                if (!(type instanceof MutableCollection))
                    System.out.println("Non MutableCollection in View = " + type.getClass().getName());
                JoriaCollection jc = (JoriaCollection) type;
                findViews(jc.getElementType(), seen, foundViews);
            }
        }
    }

    public static void add(JoriaClass f) {
        final WeakReference<JoriaClass> e = new WeakReference<JoriaClass>(f);
        extentViews.add(e);
    }
}
