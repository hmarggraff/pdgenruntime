// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.*;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;

import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLParseException;

import java.awt.Graphics2D;
import java.util.*;

public class MultilineLabelCell extends SimpleTextCellDef implements I18nKeyHolder, CellWithVariables
{
    private static final long serialVersionUID = 7L;
    Set<RuntimeParameter> variables;

    public MultilineLabelCell(TemplateModel parentGrid, String txt)
    {
        super(parentGrid, txt);
    }

    public MultilineLabelCell(MultilineLabelCell from, TemplateModel parentGrid)
    {
        super(from, parentGrid);
	    if (from.variables != null)
		    variables = new HashSet<RuntimeParameter>(from.variables);
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
    {
        return new MultilineLabelCell(this, newContainerGrid);
    }

    public void setText(String txt)
    {
        myText = txt;
        variables = null;
        grid.fireChange("multiline label change");
        Env.repoChanged();
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet)
    {
        Internationalisation2.collectI18nKeys(myText, keySet, this);
        cascadedStyle = null;
    }

    public void setI18nKey(String newVal)
    {
        myText = newVal;
        myHeight = myWidth = Float.NaN;
        grid.fireChange("New value set from I18n Key Manager");
    }

    public String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException
    {
        String merged = merge(from, into);
        return SimpleTextCellDef.wrapText(merged, getCascadedStyle(), into.getRunEnv().getLocale());
    }

    private String merge(DBData from, AggregateCollector into) throws JoriaDataException
    {
        if(myText == null)
            return null;
        int at = myText.indexOf("{");
        if (at < 0)
        {
            return myText;
        }
        StringBuffer sb = new StringBuffer();
        int lastAt = 0;
        while (at >= 0)
        {
            at++;
            if (at == 1 || myText.charAt(at - 2) != '\\')
            {
                int end = getExpression(myText, at);
                sb.append(myText, lastAt, at - 1);
                //sb.append("?[");
                final String expr = myText.substring(at, end);
                final DBData dbData = computeSubexpression(from, into.getRunEnv(), expr, getScope());
                if (dbData != null)
                    sb.append(dbData);
                lastAt = end + 1;
                //sb.append("]");
            }
            else
            {
                sb.append(myText, lastAt, at);
                lastAt = at;
            }
            at = myText.indexOf("{", lastAt);
        }
        sb.append(myText.substring(lastAt));
        return sb.toString();
    }

    public void setTagText(String s, Set<RuntimeParameter> variables)
    {
        myText = s;
        this.variables = variables;
        myWidth = Float.NaN;
        myHeight = Float.NaN;
        grid.fireChange();
    }

	public void collectVariables(Set<RuntimeParameter> v, Set<Object> seen)
    {
        if (variables != null)
            v.addAll(variables);
    }

    public boolean visitAccesses(AccessVisitor visitor, Set<JoriaAccess> seen)
    {
	    HashSet<JoriaAccess> usedAccessors = new HashSet<JoriaAccess>();
	    synchronized (this)
	    {
            getUsedAccessors(myText, getScope(), usedAccessors, visitor.stopAccessSearchOnError());
	    }
	    for (JoriaAccess joriaAccess : usedAccessors)
        {
            if (!visitor.visit(joriaAccess))
                return false;
            if (joriaAccess instanceof VisitableAccess)
            {
                if (!((VisitableAccess) joriaAccess).visitAllAccesses(visitor, seen))
                    return false;
            }
        }
        return true;
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
    {
        if (from == null || from.isNull())
            return null;
        if (repeater != null)
        {
            DBCollection source = (DBCollection) from;
            final int length = Math.max(source.getLength(), RVStringCol.startSize);
            return new RVStringCol(length);
        }
        else
        {
            if (!isVisible(outMode, from))
                return RVSupressHeader.instance;
            String v;
            try
            {
                v = getFormattedString(from, outMode.getRunEnv().getPager());
                if (v != null)
                    return new RVString(v, getCascadedStyle(), g);
            }
            catch (JoriaDataRetrievalExceptionInUserMethod e)
            {
                return new RVString(JoriaAccess.ACCESSERROR, getCascadedStyle(), g);
            }
        }
        return null;
    }

    public float getMaxWidth(RVAny values, Locale loc, Graphics2D g)
    {
        if (values == null)
            return 0;
        else if (values instanceof RVStringCol)
        {
            ((RVStringCol) values).buildFormattedStrings(this, loc);
            String[] strings = ((RVStringCol) values).get();
            float w = 0;
            CellStyle cs = getCascadedStyle();
            for (String string : strings)
            {
                if (string != null)
                {
                    if (string.indexOf('\n') >= 0)
                    {
                        char[] chars = new char[string.length()];
                        string.getChars(0, string.length(), chars, 0);
                        int ix = 0;
                        int start = 0;
                        int end = string.length();
                        while (ix < end)
                        {
                            char ch = string.charAt(ix);
                            if (ch == '\n')
                            {
                                w = Math.max(w, cs.getWidth(chars, start, ix, g));
                                start = ix + 1;
                            }
                            ix++;
                        }
                        if (start < end)
                            w = Math.max(w, cs.getWidth(chars, start, end, g));
                    }
                    else
                        w = Math.max(w, cs.getWidth(string, g));
                }
            }
            return w;
        }
        else if (values instanceof RVString)
        {
            return super.getMaxWidth(values, loc, g);
        }
        else if (values instanceof RVSupressHeader)
            return 0;
        else
            throw new JoriaAssertionError("Unhandled data value: " + values.getClass());
    }

    protected String getGraphElemString(TableBorderRequirements tblReq, int iter, FillPagedFrame out)
    {
        String s;
        if (tblReq.value != null && tblReq.value instanceof RValue) // must check if value is an RValue in case the user has put a table into the header of another table
        {
            final RValue rValue = (RValue) tblReq.value;
            s = rValue.get(iter);
        }
        else
        {
            s = null;
        }
        return s;
    }

    public void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException
    {
        super.getUsedAccessors(s);
        int at = myText.indexOf("{");
        if (at < 0)
        {
            return;
        }
        int lastAt;
        while (at >= 0)
        {
            if (at == 0 || myText.charAt(at - 1) != '\\')
            {
                at++;
                int end = getExpression(myText, at);
                final String expr = myText.substring(at, end);
                try
                {
                    final JoriaQuery q = parseSubExpression(expr, getScope());
                    q.getUsedAccessors(s);
                }
                catch (OQLParseException ex)
                {
                    ex.pos += at;
                    throw ex;
                }
                lastAt = end + 1;
            }
            else
            {
                lastAt = at + 2;
            }
            at = myText.indexOf("{", lastAt);
        }
    }
}
