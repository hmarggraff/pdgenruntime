// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.env.Res;
import org.pdgen.model.*;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FlexSize;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLParseException;
import org.pdgen.oql.OQLParser;
import org.pdgen.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectStreamException;
import java.util.List;
import java.util.*;

public class FreeCellDef implements CellDef {
    private static final long serialVersionUID = 7L;
    protected TemplateModel grid;
    protected CellStyle myStyle;
    protected Repeater repeater;
    protected transient CellStyle cascadedStyle;
    protected String visibilityCondition;
    protected transient float myWidth = Float.NaN;
    protected transient float myHeight = Float.NaN;
    /**
     * the Repeater, that this cell is a header for.
     */
    private Repeater headedRepeater;

    public FreeCellDef(FreeCellDef from, TemplateModel containerGrid) {
        Trace.check(containerGrid);
        grid = containerGrid;
        myStyle = CellStyle.duplicateLocal(from.myStyle);
        visibilityCondition = from.visibilityCondition;
    }

    public FreeCellDef(TemplateModel containerGrid) {
        Trace.check(containerGrid);
        grid = containerGrid;
    }

    public FreeCellDef(TemplateModel containerGrid, Repeater repeater) {
        grid = containerGrid;
        this.repeater = repeater;
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences) {
        return new FreeCellDef(this, newContainerGrid);
    }

    protected void adjustSize(Locale loc, Graphics2D g) {
        cascadeStyle();
        myWidth = cascadedStyle.getLeftRightPaddingValue();
        myHeight = cascadedStyle.getTopBotPaddingValue();
    }

    protected void postCalcSize(Locale loc) {
        ImageIcon picture = cascadedStyle.getBackgroundImage(loc);
        if (picture != null) {
            float width = picture.getIconWidth();
            float height = picture.getIconHeight();
            FlexSize scaleSize = cascadedStyle.getBackgroundImageTargetWidth();
            if (scaleSize != null && !scaleSize.isExpandable()) {
                width = scaleSize.getVal();
                height = scaleSize.getVal() / width * height;
            }
            myWidth = Math.max(myWidth, width);
            myHeight = Math.max(myHeight, height);
        }
    }

    /**
     * ensures cascadedStyle is non null and containsName no nulls
     * must be called before cascaded style is used for formatting
     */
    public final synchronized void cascadeStyle() {
        if (cascadedStyle != null)
            return;
        // Trace.logDebug(4,"cascadeStyle " + super.toString());
        cascadedStyle = new CellStyle(myStyle);
        cascadedStyle.fillFromBase(cascadedStyle.getBaseStyle());
        grid.getFrame().cascadeCellStyle(cascadedStyle);
        // restict span to effective value
        int hspan = cascadedStyle.calcSpanHorizontal();
        int vspan = cascadedStyle.calcSpanVertical();
        if (hspan > 1 || vspan > 1) {
            Point pos = grid.getCellPosition(this);
            int row = pos.y;
            int col = pos.x;
            int newHSpan = trimHSpan(hspan, row, col, vspan);
            if (newHSpan != hspan)
                cascadedStyle.setSpanHorizontal(newHSpan);
            int newVSpan = trimVSpan(vspan, row, col, newHSpan);
            if (newVSpan != vspan)
                cascadedStyle.setSpanVertical(newVSpan);
        }
    }

    public int trimHSpan(int newHSpan, int row, int col, int newVSpan) {
        final int maxCol;
        if (repeater != null) {
            maxCol = repeater.getEndCol();
        } else {
            maxCol = grid.getEndCol();
        }
        if (col + newHSpan - 1 > maxCol)
            newHSpan = maxCol - col + 1;
        for (int r = row; r < row + newVSpan; r++) {
            for (int c = col; c < col + newHSpan; c++) {
                if (grid.getRepeaterList().innerMostRepeaterAt(r, c) != repeater) {
                    newHSpan = c - col;
                    break;
                }
            }
        }
        if (newHSpan == 0)
            newHSpan = 1;
        return newHSpan;
    }

    public int trimVSpan(int newVSpan, int row, int col, int newHSpan) {
        final int maxRow;
        if (repeater != null) {
            maxRow = repeater.getEndRow();
        } else {
            maxRow = grid.getEndRow();
        }
        if (row + newVSpan - 1 > maxRow) {
            newVSpan = maxRow - row + 1;
        }
        RepeaterList repList = grid.getRepeaterList();
        for (int c = 0; c < grid.getColCount(); c++) {
            Repeater rep = repList.innerMostRepeaterAt(row, c);
            if (rep == null)
                continue;
            if (rep.getEndRow() < row + newVSpan - 1)
                newVSpan = rep.getEndCol() - row + 1;
        }
        for (int c = col; c < col + newHSpan; c++) {
            for (int r = row; r < row + newVSpan; r++) {
                if (grid.getRepeaterList().innerMostRepeaterAt(r, c) != repeater) {
                    newVSpan = r - row;
                    break;
                }
            }
        }
        return newVSpan;
    }

    public CellStyle getCascadedStyle() {
        if (cascadedStyle != null)
            return cascadedStyle;
        cascadeStyle();
        return cascadedStyle;
    }

    public Component getComponent() {
        return null;
    }

    public TemplateModel getGrid() {
        return grid;
    }

    public float getHeight(Locale loc, Graphics2D g) {
        if (Float.isNaN(myHeight))
            adjustSize(loc, g);
        return myHeight;
    }

    public Repeater getRepeater() {
        return repeater;
    }

    public CellStyle getStyle() {
        return myStyle;
    }

    public float getWidth(Locale loc, Graphics2D g) {
        if (Float.isNaN(myWidth)) {
            adjustSize(loc, g);
            postCalcSize(loc);
        }
        return myWidth;
    }

    protected Object readResolve() throws ObjectStreamException {
        cascadedStyle = null;
        myWidth = Float.NaN;
        myHeight = Float.NaN;
        return this;
    }

    public void setGrid(TemplateModel parentGrid) {
        grid = parentGrid;
    }

    public void setRepeater(Repeater newRepeater) {
        repeater = newRepeater;
    }

    public void setStyle(CellStyle sty) {
        Trace.logDebug(Trace.edit, "setStyle \"" + (sty != null ? sty.getName() : "null") + "\"");
        myStyle = sty;
        clearCachedStyle();
    }

    public void clearCachedStyle() {
        cascadedStyle = null;// release cached style
        myWidth = Float.NaN;
        myHeight = Float.NaN;
        grid.fireChange("cached cell style cleared");//trdone
    }

    public String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException {
        return null;
    }

    public void removed() {
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
    }

    public void paintBackGround(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        ImageIcon picture = cascadedStyle.getBackgroundImage(loc);
        if (picture != null) {
        }
    }

    public boolean isReflowing() {
        return false;
    }

    public void reFlow(float width, Locale loc, Graphics2D g) {
        throw new JoriaAssertionError("called reflow for a cell that does not allow reflowing");
    }

    public String getVisibilityCondition() {
        return visibilityCondition;
    }

    public void setVisibilityCondition(String visibilityCondition) {
        this.visibilityCondition = visibilityCondition;
        grid.fireChange("visibility condition");//trdone
    }

    public Set<RuntimeParameter> getVisibilityConditionVars() {
        JoriaType scope = getScope();
        try {
            return OQLParser.checkForVars(visibilityCondition, scope);
        } catch (Exception ex) {
            throw new JoriaInternalError("Unexpected Parse Exception in run", ex);
        }
    }

    public Set<JoriaAccess> getVisibilityConditionUsedAccessors() throws OQLParseException {
        JoriaType scope = getScope();
        return OQLParser.checkForUsedAccessors(visibilityCondition, scope);
    }

    public void checkVisibilityCondition() {
        if (StringUtils.trimNull(visibilityCondition) == null)
            return;
        JoriaType scope = getScope();
        try {
            OQLParser.checkForVars(visibilityCondition, scope);
        } catch (Exception ex) {
            try {
                ex.printStackTrace();
                OQLParser.checkForVars(visibilityCondition, scope);
            } catch (Throwable e) {
                Trace.log(e);
            }
            Env.instance().repo().logFix(Res.str("VisibilityConditionOnCell"), visibilityCondition, Res.str("Visibility_condition_deactivated"));
            visibilityCondition = null;// TODO bessere Loesung notwendig
        }
    }

    public JoriaQuery makeVisibilityQuery() {
        if (visibilityCondition == null)
            return null;
        JoriaQuery q;
        try {
            q = OQLParser.parse(visibilityCondition, getScope());
        } catch (Exception ex) {
            throw new JoriaInternalError("Unexpected Parse Exception in run", ex);
        }
        return q;
    }

    public boolean isVisible(OutputMode env, DBData from) throws JoriaDataException {
        JoriaQuery q = makeVisibilityQuery();
        return q == null || q.getBooleanValue(env.getRunEnv(), from);
    }

    public void collectI18nKeys(HashSet<String> keySet) {
        collectI18nKeysInLocalStyle(keySet);
    }

    public void collectI18nKeysInLocalStyle(HashSet<String> keySet) {
        if (myStyle != null && myStyle.getName() == null)
            myStyle.collectI18nKeys(keySet);
    }

    public void collectI18nKeysInLocalStyle2(HashMap<String, List<I18nKeyHolder>> keySet) {
        if (myStyle != null && myStyle.getName() == null)
            myStyle.collectI18nKeys2(keySet, this);
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet) {
        collectI18nKeysInLocalStyle2(keySet);
    }

    public Repeater getHeadedRepeater() {
        return headedRepeater;
    }

    public void setHeadedRepeater(Repeater headed) {
        headedRepeater = headed;
    }

    public static int getSpanEnd(CellDef cd, int pos, boolean horizontal) {
        if (cd == null)
            return pos;
        if (horizontal) {
            pos += cd.getCascadedStyle().getSpanHorizontal() - 1;
            Repeater r = cd.getRepeater();
            if (r != null)
                pos = Math.min(pos, r.getEndCol());
            else
                pos = Math.min(pos, cd.getGrid().getColCount() - 1);
        } else {
            pos += cd.getCascadedStyle().getSpanVertical() - 1;
            Repeater r = cd.getRepeater();
            if (r != null)
                pos = Math.min(pos, r.getEndRow());
            else
                pos = Math.min(pos, cd.getGrid().getRowCount() - 1);
        }
        return pos;
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        return null;
    }

    public ModelBase getModel() {
        return grid;
    }

    public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException {
        return out.makeEmptyGrel(tblReq);
    }

    public float getMaxWidth(RVAny values, Locale loc, Graphics2D g) {
        return 0;
    }

    public float heightForWidth(float width, Locale loc, Graphics2D g) {
        return getHeight(loc, g);
    }

    public JoriaClass getScope() {
        if (repeater != null)
            return repeater.getAccessor().getSourceCollection().getElementType();
        final JoriaAccess rootAxs = getGrid().getFrame().getPageLevelParent().getRoot();
        if (rootAxs != null)
            return (JoriaClass) rootAxs.getType();
        else
            return null; // unbound reports
    }

    public void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException {
        OQLParser.getUsedAccessors(s, visibilityCondition, getScope());
    }

    public void makeUnboundCell() {
    }

    public void resetLayout() {
        myHeight = Float.NaN;
        myWidth = Float.NaN;
        if (isReflowing()) {
            grid.getLayouter().getTopContainer().invalidateLayout();
        }
    }

    public String getCellKind() {
        return StringUtils.removePackagesFromName(getClass().toString());
    }

    public CellStyle getConditionalStyle(DBData val) {
        return getCascadedStyle();
    }

    public boolean hasText(final String text) {
        if (visibilityCondition == null)
            return false;
        return visibilityCondition.toLowerCase().contains(text);
    }

    public static DBData computeSubexpression(DBData from, RunEnv env, String expression, JoriaClass scope) throws JoriaDataException {
        if (StringUtils.isEmpty(expression))
            return null;
        JoriaQuery query;
        try {
            query = parseSubExpression(expression, scope);
        } catch (OQLParseException ex) {
            throw new JoriaAssertionError("Undetected syntax error in formula " + ex.getMessage() + " at " + ex.pos + " in " + ex.query);
        }
        return query.getValue(env, from);
    }

    public static void getUsedAccessors(String expression, JoriaClass scope, Set<JoriaAccess> accessors, final boolean stopOnError) {
        if (expression == null)
            return;
        int at = expression.indexOf("{");
        if (at < 0)
            return;
        while (at >= 0) {
            at++;
            final int lastAt;
            if (at == 1 || expression.charAt(at - 2) != '\\') {
                int end = getExpression(expression, at);
                final String expr = expression.substring(at, end);
                try {
                    JoriaQuery query = parseSubExpression(expr, scope);
                    query.getUsedAccessors(accessors);
                } catch (OQLParseException e) {
                    if (stopOnError)
                        throw new JoriaAssertionError("Undetected syntax error in formula " + e.getMessage() + " at " + e.pos + " in " + e.query);
                }
                lastAt = end + 1;
            } else {
                lastAt = at;
            }
            at = expression.indexOf("{", lastAt);
        }
    }

    public static JoriaQuery parseSubExpression(String expression, JoriaClass parent) throws OQLParseException {
        JoriaQuery query = OQLParser.parse(expression, parent, false);
        return query;
    }

    public static int getExpression(CharSequence txt, int at) {
        boolean toplevel = true;
        while (at < txt.length()) {
            final char c = txt.charAt(at);
            if (c == '\\') {
                at++;
            } else if (c == '}' && toplevel) {
                return at;
            } else if (c == '"') {
                toplevel = !toplevel;
            }
            at++;
        }
        return -1;
    }
}
