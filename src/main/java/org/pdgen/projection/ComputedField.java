// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;

import org.pdgen.data.*;
import org.pdgen.data.view.*;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaUserDataException;
import org.pdgen.env.JoriaUserError;
import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.*;
import org.pdgen.schemacheck.CheckFormulasForSchemaChange;
import org.pdgen.util.StringUtils;

import java.util.*;

public class ComputedField extends AbstractTypedJoriaAccess implements NameableAccess, VariableProvider, VisitableAccess, JoriaAccess {
    private static final long serialVersionUID = 7L;
    String expression;
    JoriaClass parent;
    protected String xmlTag;
    boolean exportingAsXmlAttribute;
    String formatString;
    boolean isImage;
    transient boolean definedInView;// marks formulas, that are definhed in a view and not locally in a report.

    public ComputedField(JoriaAccess displayField, JoriaClass parent) {
        this.parent = parent;
        xmlTag = expression = displayField.getName();
        type = displayField.getType();
        name = Res.strp("formulaUnderscore", displayField.getName());
        JoriaAccess member = parent.findMember(name);
        int cnt = 1;
        while (member != null && member != this) {
            name = Res.strp("formula", cnt) + "_" + displayField.getName();
            member = parent.findMember(name);
        }
    }

    public NameableAccess dup(JoriaClass newParent, Map<Object, Object> alreadyCopied) {
        final Object duplicate = alreadyCopied.get(this);
        if (duplicate != null)
            return (NameableAccess) duplicate;
        ComputedField ret = new ComputedField(name, newParent);
        alreadyCopied.put(this, ret);
        ret.expression = expression;        //ret.variables = variables;
        ret.xmlTag = xmlTag;
        ret.exportingAsXmlAttribute = exportingAsXmlAttribute;
        ret.formatString = formatString;
        ret.type = type;
        return ret;
    }

    public ComputedField(String name, JoriaClass parent) {
        super(name);
        this.parent = parent;
        type = DefaultStringLiteral.instance();
        makeLongName();
    }

    public JoriaClass getDefiningClass() {
        return parent;
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        if (StringUtils.isEmpty(expression))
            return null;
        try {
            JoriaQuery query = getQuery();
            DBData d = query.getValue(env, from);
            if (d instanceof DBCollection) {
                OQLNode queryNode;
                JoriaCollection t1 = asView.getSourceCollection();
                SortOrder[] sortRules = AbstractJoriaAccess.getSorting(t1, env);
                int topN = t1.getMinTopN(env);
                try {
                    queryNode = asView.getCascadedOQLFilter();
                } catch (OQLParseException ex) {
                    throw new JoriaDataException(ex.getMessage());
                }
                TopNBuilder tb = null;
                if (topN > 0) {
                    if (sortRules != null)
                        tb = new TopNBuilder(topN, sortRules, env);
                } else
                    topN = Integer.MAX_VALUE;
                while (t1.isView() && t1.getElementMatchType() == null) {
                    CollectionView v = (CollectionView) t1;
                    t1 = v.getBaseCollection();
                }
                JoriaClass mt = t1.getElementMatchType();
                if (queryNode != null || topN != Integer.MAX_VALUE || sortRules != null || mt != null) {
                    DBCollection collVal = (DBCollection) d;
                    ArrayList<DBObject> out = new ArrayList<DBObject>();
                    while (collVal.next()) {
                        DBObject ed = collVal.current();
                        if ((mt == null || JoriaClassHelper.isAssignableFrom(ed, mt)) && (queryNode == null || queryNode.getBooleanValue(env, ed))) {
                            if (tb != null)
                                tb.addTopN(out, ed);
                            else {
                                out.add(ed);
                                if (queryNode == null && out.size() >= topN)
                                    break;
                            }
                        }
                    }
                    collVal.reset();
                    if (out.size() == 0)
                        return null;
                    if (sortRules != null && tb == null)// only sort if not topN, because topN did already sort
                    {
                        ProjectionComparator comp = new ProjectionComparator(sortRules, env);
                        Collections.sort(out, comp);
                    }
                    d = new FilteredDBCollection(out, asView, asView.getType());
                }
            }
            if (isImage && d != null) {
                if (!(d instanceof DBString))
                    throw new JoriaAssertionError("only string typed queries can be images");
                DBString ds = (DBString) d;
                d = new DBImage(ds.getStringValue(), this);
            }
            return d;
        } catch (OQLParseException ex) {
            throw new JoriaUserDataException("Undetected syntax error in formula:" + getName() + "\n" + ex.getMessage() + "\nat " + ex.pos + " in\n" + ex.query + "\n", ex);
        }
    }

    public JoriaQuery getQuery() throws OQLParseException {
        JoriaQuery query = OQLParser.parse(expression, parent, false);
        return query;
    }

    public boolean isRoot() {
        return false;
    }

    public void setName(String newName) {
        NameableTracer.notifyListenersPre(this);
        name = newName;
        makeLongName();
        NameableTracer.notifyListenersPost(this);
    }

    public void setExpression(String newDef, JoriaType type) {
        if (newDef == null || "".equals(newDef))
            this.type = DefaultStringLiteral.instance();
        else
            this.type = type;
        if (isImage) {
            if (this.type.isStringLiteral()) {
                this.type = DefaultImageLiteral.instance();
            } else
                throw new JoriaAssertionError("only string typed queries can be images");
        }
        OQLParser.clearFromCache(expression, parent);
        expression = newDef;        //variables = vars;
        makeLongName();
    }

    public String getFilter() {
        return expression;
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen) {
        try {
            final Set<RuntimeParameter> variables = getQuery().getVariables();
            RuntimeParameter.addAll(s, variables, seen);
            if (type instanceof VariableProvider)
                ((VariableProvider) type).collectVariables(s, seen);
        } catch (OQLParseException ex) {
            throw new JoriaUserError("Undetected syntax error in formula:" + getName() + "\n" + ex.getMessage() + "\nat " + ex.pos + " in\n" + ex.query + "\n", ex);
        }
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet, Set<Object> seen) {
        if (expression != null) {
            Env.instance().collectI18nKeys(expression, parent, false, Res.strp("Computed_Field", parent.getName()) + "." + getName(), keySet);
        }
        if (type instanceof VariableProvider)
            ((VariableProvider) type).collectI18nKeys2(keySet, seen);
    }

    public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen) {
        if (seen.contains(this))
            return;
        else
            seen.add(this);
        if (type instanceof VariableProvider)
            ((VariableProvider) type).collectVisiblePickersInScope(collection, visible, pathStack, seen);
    }

    public String toString() {
        return name;
    }

    public JoriaType getSourceTypeForChildren() {
        return getType();
    }

    public void makeName() {
        makeLongName();
    }

    public String getXmlTag() {
        return xmlTag;
    }

    public void setXmlTag(String xmlTag) {
        this.xmlTag = xmlTag;
    }

    public boolean isExportingAsXmlAttribute() {
        return exportingAsXmlAttribute;
    }

    public void setExportingAsXmlAttribute(boolean exportingAsXmlAttribute) {
        this.exportingAsXmlAttribute = exportingAsXmlAttribute;
    }

    public JoriaAccess getPlaceHolderIfNeeded() {
        if (parent instanceof JoriaUnknownType) {
            Env.instance().repo().logFix(Res.str("Formula"), this, Res.msg("deactivated_scope_class_was_removed_0__expression_was_1", parent.getName(), expression));
            return new JoriaPlaceHolderAccess(name, expression);
        }
        try {
            JoriaQuery query = getQuery();
            if (query.hasMofifiedAccess()) {
                Env.instance().repo().logFix(Res.strb("Formula"), this, Res.strp("deactivated_expression_was", expression));
                return new JoriaPlaceHolderAccess(name, expression);
            }
        } catch (OQLParseException ex) {
            Trace.log(ex);            //ex.printStackTrace();
            Env.instance().repo().logFix("Formula ", this, Res.strp("deactivated_expression_was", expression));//trdone
            return new JoriaPlaceHolderAccess(name, expression + Res.stricb("at") + ex.pos);
        }
        return null;
    }

    public JoriaType getType() {
        if (type != null) {
            if (isImage)
                return DefaultImageLiteral.instance();
            else
                return type;
        }
        try {
            JoriaQuery query = getQuery();
            if (query.hasMofifiedAccess())
                type = JoriaUnknownType.createJoriaUnknownType("TypeOf " + expression);//trdone
            else
                type = query.getType();
            if (isImage)
                return DefaultImageLiteral.instance();
            return type;
        } catch (OQLParseException ex) {
            Trace.log(ex);
            return JoriaUnknownType.createJoriaUnknownType("TypeOf " + expression);//trdone
        }
    }

    public JoriaAccess getSimplifiedAccess() throws OQLParseException {
        JoriaQuery query = getQuery();
        JoriaAccess simpleAccess = query.getSimpleAccess();
        if (simpleAccess != null)
            return simpleAccess;
        else
            return this;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public boolean isPageRel() {
        try {
            JoriaQuery query = getQuery();
            return query instanceof JoriaDeferedQuery;
        } catch (OQLParseException e) {
            Env.instance().handle(e);
            return false;
        }
    }

    public boolean isNeedTotalPages() {
        try {
            JoriaQuery query = getQuery();
            return query instanceof JoriaDeferedQuery && ((JoriaDeferedQuery) query).isNeedsAllPages();
        } catch (OQLParseException e) {
            Env.instance().handle(e);
            return false;
        }
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        if (!getType().isStringLiteral() && !getType().isImage() && image)
            throw new JoriaAssertionError("only string typed queries can be images");
        isImage = image;
    }

    public boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen) {
        if (seen.contains(this))
            return true;
        seen.add(this);
        try {
            JoriaQuery query = getQuery();
            Set<JoriaAccess> axss = new HashSet<JoriaAccess>();
            query.getUsedAccessors(axss);
            for (JoriaAccess axs : axss) {
                if (!visitor.visit(axs))
                    return false;
            }
        } catch (OQLParseException e) {
            final String parentName = parent.getName();
            System.out.println("Field: " + getLongName() + " in class " + parentName + " can not be parsed. " + e.getMessage() + " Deactivated. Expression= " + expression);
            expression = "null\n//Formula commented out, because a schema change made it unusable.\n/*\n" + expression + "\n*/";
        }
        return true;
    }

    public boolean isDefinedInView() {
        return definedInView;
    }

    public void setDefinedInView(final boolean definedInView) {
        this.definedInView = definedInView;
    }

    /**
     * Setting the parent must be done with care. Currently only if a template local formula is
     * put into a new view for the template
     *
     * @param view the parent view
     */
    public void setParent(final JoriaClass view) {
        parent = view;
    }

    public int hashCode() {
        return expression.hashCode();
    }

    public static JoriaDeferedQuery getIfDeferredQuery(final boolean needsAllPages, final JoriaAccess axs) {
        if (axs instanceof ComputedField) {
            ComputedField cf = (ComputedField) axs;
            try {
                JoriaQuery query = cf.getQuery();
                if (query instanceof JoriaDeferedQuery) {
                    JoriaDeferedQuery deferedQuery = (JoriaDeferedQuery) query;
                    if (deferedQuery.isNeedsAllPages() == needsAllPages)
                        return deferedQuery;
                }
            } catch (OQLParseException e) {
                Env.instance().handle(e);
            }
        }
        return null;
    }

    protected Object readResolve() {
        CheckFormulasForSchemaChange.allComputedFields.add(this);
        return this;
    }

}
