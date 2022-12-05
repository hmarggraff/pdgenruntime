// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.DBData;
import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.ConditionalVisibility;
import org.pdgen.model.RDBase;
import org.pdgen.model.Repeater;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.oql.OQLParseException;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;

public interface CellDef extends Serializable, RDBase, ConditionalVisibility {
    CellStyle getCascadedStyle();

    TemplateModel getGrid();

    float getHeight(Locale loc, Graphics2D g);

    Repeater getRepeater();

    CellStyle getStyle();

    float getWidth(Locale loc, Graphics2D g);

    void setGrid(TemplateModel parentGrid);

    void setRepeater(Repeater newRepeater);

    void setStyle(CellStyle sty);

    String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException;

    /**
     * is called when cell is removed to do cleanup work. Not called by TemplateMode.removeToo to avoid cycles.
     */
    void removed();

    void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc);

    void clearCachedStyle();

    boolean isReflowing();

    void reFlow(float width, Locale loc, Graphics2D g);

    /*
     * is called when a report is duplicated *
     */
    CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences);

    boolean isVisible(OutputMode env, DBData from) throws JoriaDataException;

    float getMaxWidth(RVAny values, Locale loc, Graphics2D g);

    boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException;

    void collectI18nKeys(HashSet<String> keySet);

    void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet);

    /**
     * if this cell is the header for a repeater, return the coresponding RDRepeater
     *
     * @return the Repeater, that this cell is the header for
     */
    Repeater getHeadedRepeater();

    /**
     * sets the Repeater that this cell is the header for
     * the headed repeater is transient and calculated before a run.
     *
     * @param headed the Rrepeater that this cell am the header for
     */
    void setHeadedRepeater(Repeater headed);

    int trimHSpan(int newHSpan, int row, int col, int newVSpan);

    int trimVSpan(int newVSpan, int row, int col, int newHSpan);

    void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException;

    void makeUnboundCell();

    void resetLayout();

    String getCellKind();

    CellStyle getConditionalStyle(DBData val);

    boolean hasText(final String text);

    enum Occurence {
        text,
        field,
        visibilityCondition,
    }
}
