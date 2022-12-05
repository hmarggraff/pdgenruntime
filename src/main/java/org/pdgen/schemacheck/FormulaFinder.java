// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.schemacheck;

import org.pdgen.data.*;
import org.pdgen.data.view.*;
import org.pdgen.env.Env;
import org.pdgen.model.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.StyledTextCellDef;
import org.pdgen.projection.ComputedField;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class FormulaFinder implements AccessVisitor, RepeaterVisitor, CellVisitor, FrameVisitor {
    protected final Set<FormulaContext> oqlObjects = new HashSet<FormulaContext>();
    Template currTemplate;

    public static Set<FormulaContext> findAllOQLObjects() {
        final FormulaFinder finder = new FormulaFinder();
        finder.collectOQLObjects();
        return finder.oqlObjects;
    }

    public static Set<FormulaContext> findAllFormulasInViews() {
        final FormulaFinder finder = new FormulaFinder();
        finder.findAllFormulasInViews1();
        return finder.oqlObjects;
    }

    private void collectOQLObjects() {
        final Repository repos = Env.instance().repo();
        HashSet<JoriaAccess> seen = new HashSet<JoriaAccess>();
        for (Template r : repos.reports.getData()) {
            currTemplate = r; // hold, so that visitors can reference it
            r.getPage().visitAllAccessors(this, seen);
            r.visitRepeaters(this);
            r.getPage().visitCells(this);
            r.visitFrames(this);
            if (r.getReportType() == Template.masterDetailReport) {
                final JoriaAccess a = r.getData();
                JoriaCollection coll;
                if (a == null) // unbound template
                    continue;
                else if (a instanceof AccessPath) {
                    AccessPath ap = (AccessPath) a;
                    coll = ap.getSourceCollection();
                } else
                    coll = a.getSourceCollection();
                if (coll.getFilter() != null)
                    addOqlContext(coll, r, "MultiDocument Collection Filter");
                System.currentTimeMillis();
            }
        }
        findAllFormulasInViews1();
        /// query parameters (aka variables)
        for (RuntimeParameter v : repos.variables.getData()) {
            Object oqlEvaluator = v.getOqlEvaluator();
            if (oqlEvaluator == null) {
                addOqlContext(oqlEvaluator, v, "Root Collection Filter of RuntimeParameter");
            }
        }
    }

    void addOqlContext(Object holder, Object context, String explanation) {
        oqlObjects.add(new FormulaContext(holder, context, explanation));
    }

    public void findAllFormulasInViews1() {
        final Repository repos = Env.instance().repo();
        // root views
        final HashSet<JoriaClass> seenClasses = new HashSet<JoriaClass>();
        final Stack<String> path = new Stack<String>();
        for (CastAccess a : repos.userRoots.getData()) {
            final JoriaClass tClass = (JoriaClass) a.getType();
            path.push(a.getName());
            findComputedFieldsInClassView(tClass, seenClasses, path);
            path.pop();
        }

        // class and collection views
        for (ClassProjection v : repos.classProjections) {
            path.push("ViewOf_" + v.getBase().getName());
            path.push(v.getName());
            findComputedFieldsInClassView(v, seenClasses, path);
            path.pop();
        }
        for (CollectionProjection v : repos.collectionProjections) {
            path.push("ViewOf_" + v.getBase().getName());
            path.push(v.getName());
            findComputedFieldsInClassView(v.getElementType(), seenClasses, path);
            path.pop();
        }
    }

    void findComputedFieldsInClassView(final JoriaClass tClass, Set<JoriaClass> seen, Stack<String> path) {
        if (seen.contains(tClass))
            return;
        seen.add(tClass);
        final JoriaAccess[] members = tClass.getMembers();
        if (members == null)
            return;
        for (JoriaAccess member : members) {
            final JoriaType type = member.getType();
            if (type.isClass()) {
                if (!(type instanceof ClassView || type instanceof JoriaDateTime))
                    System.out.println("FormulaCheck: Non View in View = " + type.getClass().getName());
                path.push(member.getName());
                findComputedFieldsInClassView((JoriaClass) type, seen, path);
                path.pop();
            } else if (type.isCollection()) {
                if (!(type instanceof MutableCollection))
                    System.out.println("Non MutableCollection in View = " + type.getClass().getName());
                JoriaCollection jc = (JoriaCollection) type;
                final Filter filter = jc.getFilter();
                if (filter != null)
                    addOqlContext(jc, pathString(path, jc), "Collection filter in view");
                path.push(member.getName());
                findComputedFieldsInClassView(jc.getElementType(), seen, path);
                path.pop();
            }
            if (member instanceof ComputedField)
                addOqlContext(member, pathString(path, member), "Formula in view");
        }
    }

    private String pathString(final Stack<String> path, Named last) {
        StringBuffer ret = new StringBuffer();
        for (String s : path) {
            ret.append(s);
            ret.append('.');
        }
        ret.append(last.getName());
        return ret.toString();
    }

    public boolean visit(final CellDef cd, final int r, final int c) {
        if (cd != null) {
            if (cd instanceof StyledTextCellDef)
                addOqlContext(cd, new CellContext(currTemplate, cd.getModel().getFrame().getTopLevelParent(), r, c), "Styled Text Cell");
            if (cd.getVisibilityCondition() != null)
                addOqlContext(cd, new CellContext(currTemplate, cd.getModel().getFrame().getTopLevelParent(), r, c), "Visibility Condition of Cell");
        }
        return true;
    }

    public boolean visit(final Repeater r) {
        final JoriaCollection collection = r.getAccessor().getCollectionTypeAsserted();
        if (collection.getFilter() != null)
            addOqlContext(r, currTemplate, "Filter of Repeater");
        return true;
    }

    public boolean visitFrame(final TemplateBox frame) {
        if (frame.getVisibilityCondition() != null)
            addOqlContext(frame, currTemplate, "Visibiliy Condition of Frame");
        return true;
    }

    public boolean visit(JoriaAccess access) {
        if (access instanceof ComputedField)
            addOqlContext(access, currTemplate, "Other formula");
        return true;
    }

    public boolean stopAccessSearchOnError() {
        return false;
    }
}