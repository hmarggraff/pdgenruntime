// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.oql.OQLParseException;
import org.pdgen.projection.UnboundMembersClass;

import java.awt.*;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class CrosstabValueCellDef extends SimpleTextCellDef implements CrosstabCell {
    private static final long serialVersionUID = 7L;
    protected JoriaAccess valueGetter;
    int aggFunction;  // enum values defined in AggregateDef

    public CrosstabValueCellDef(TemplateModel parentGrid, JoriaAccess vg, int aggFunction) {
        super(parentGrid, calcName(vg, aggFunction));
        valueGetter = vg;
        this.aggFunction = aggFunction;
    }

    public CrosstabValueCellDef(CrosstabValueCellDef from, TemplateModel parentGrid) {
        super(from, parentGrid);
        valueGetter = from.valueGetter;
        aggFunction = from.aggFunction;
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences) {
        return new CrosstabValueCellDef(this, newContainerGrid);
    }

    public JoriaAccess getAccessor() {
        return valueGetter;
    }

    public String toString() {
        if (valueGetter != null)
            return "CrosstabValueCellDef(" + valueGetter.getLongName() + ":" + AggregateDef.tagStrings[aggFunction] + ")";
        else
            return "CrosstabValueCellDef(no value:" + AggregateDef.tagStrings[aggFunction] + ")";
    }

    public String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException {
        DBData cv = valueGetter.getValue(from, valueGetter, into.getRunEnv());
        if (cv == null)
            return null;
        return SimpleTextCellDef.wrapText(into.format(cv, getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
    }

    public float getWidth(Locale loc, Graphics2D g) {
        float w = super.getWidth(loc, g);
        Trace.check(w > 0.01, "DataCelldef with size 0");
        return w;
    }

    public void fixAccess() {
        if (valueGetter == null)
            return;
        JoriaAccess fixed = valueGetter.getPlaceHolderIfNeeded();
        if (fixed != null)
            valueGetter = fixed;
        if (fixed instanceof JoriaPlaceHolderAccess || fixed instanceof JoriaModifiedAccess)
            makeModified();
    }

    public void makeModified() {
        valueGetter = UnboundMembersClass.getUnboundLiteral();
        myText = "?" + valueGetter.getName() + " " + myText;
        CellStyle ps = new CellStyle();
        ps.setBackground(Color.orange);
        myStyle = ps;
    }

    public void makeName() {
        myText = calcName(valueGetter, aggFunction);
        myWidth = Float.NaN;
        myHeight = Float.NaN;
        grid.fireChange("DataCellDef displayName changed");
    }

    static String calcName(JoriaAccess ax, int function) {
        if (ax == null) {
            Trace.check(function == AggregateDef.len);
            return "count";
        } else {
            return AggregateDef.tagStrings[function] + "(" + ax.getName() + ")";
        }
    }

    public String getAccessorName() {
        return valueGetter.getLongName();
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        throw new JoriaAssertionError("this should never be called");
    }

    public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException {
        if (tblReq.value != null) {
            String s = ((RValue) tblReq.value).get(iter);
            if (s != null) {
                return out.makeTextGraphEl(tblReq, s);
            } else
                return out.makeEmptyGrel(tblReq);
        } else
            return out.makeEmptyGrel(tblReq);
    }

    public float getMaxWidth(RVAny values, Locale loc, Graphics2D g) {
        if (values == null)
            return 0;
        else if (values instanceof RVStringCol) {
            ((RVStringCol) values).buildFormattedStrings(this, loc);
            String[] strings = ((RVStringCol) values).get();
            float w = 0;
            CellStyle cs = getCascadedStyle();
            //exp hmf 040924: We want to know the length for reflowed text.
            //if (cs.getTextType().intValue() == CellStyle.reFlowType.intValue() || cs.getTextType().intValue() == CellStyle.htmlType.intValue())
            //return 0;
            for (String string : strings) {
                if (string != null) {
                    if (string.indexOf('\n') >= 0) {
                        char[] chars = new char[string.length()];
                        string.getChars(0, string.length(), chars, 0);
                        int ix = 0;
                        int start = 0;
                        int end = string.length();
                        while (ix < end) {
                            char ch = string.charAt(ix);
                            if (ch == '\n') {
                                w = Math.max(w, cs.getWidth(chars, start, ix, g));
                                start = ix + 1;
                            }
                            ix++;
                        }
                        if (start < end)
                            w = Math.max(w, cs.getWidth(chars, start, end, g));
                    } else
                        w = Math.max(w, cs.getWidth(string, g));
                }
            }
            return w;
        } else if (values instanceof RVString) {
            return super.getMaxWidth(values, loc, g);
        } else
            throw new JoriaAssertionError("Unhandled data value: " + values.getClass());
    }

    public void makeUnboundCell() {
        valueGetter = UnboundMembersClass.getUnboundLiteral();
        myText = valueGetter.getName();
    }

    public void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException {
        super.getUsedAccessors(s);
        s.add(valueGetter);
    }
}
