// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.model.ModelBase;
import org.pdgen.model.NestedBox;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.NestingCellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FlexSize;

import java.awt.*;
import java.io.Serializable;
import java.util.Locale;

public abstract class RDRangeBase implements RDBase, Serializable {
    private static final long serialVersionUID = 7L;
    protected ModelBase myModel;
    protected RDBase[][] fields;

    protected RDRangeBase(ModelBase model, RDBase[][] fields) {
        myModel = model;
        this.fields = fields;
    }

    public JoriaAccess getAccess() {
        return myModel.getAccessor();
    }

    public ModelBase getModel() {
        return myModel;
    }

    public RDBase[][] getFields() {
        return fields;
    }

    public void calcMaxWidth(float[][] widths, RVAny values, Locale loc, Graphics2D g) {
        RVTemplate v = (RVTemplate) values;
        if (v == null)
            return;
        int rBase;
        if (myModel == null)
            rBase = 0;
        else
            rBase = myModel.getStartRow();
        for (int r = 0; r < fields.length; r++) {
            RDBase[] row = fields[r];
            int wPos;
            if (myModel == null)
                wPos = 0;
            else
                wPos = myModel.getStartCol();
            for (int c = 0; c < row.length; c++) {
                RDBase rdb = row[c];
                if (rdb == null) {
                    FlexSize cfs = myModel.getColSizingAt(wPos - myModel.getStartCol());
                    if (cfs != null && !cfs.isExpandable()) {
                        widths[r + rBase][wPos] = cfs.getVal();
                    }
                    wPos++;
                    continue;
                }
                RVAny vv = v.get(r, c);
                if (rdb instanceof RDRepeater) {
                    RDRepeater rdr = (RDRepeater) rdb;
                    if (vv instanceof RVObjects) {
                        RVObjects vvo = (RVObjects) vv;
                        for (int vvox = 0; vvox < vvo.elementCount(); vvox++) {
                            rdr.calcMaxWidth(widths, vvo.elems[vvox], loc, g);
                        }
                    } else
                        rdr.calcMaxWidth(widths, vv, loc, g);
                    wPos += rdr.getModel().getColCount();
                } else {
                    CellDef cd;
                    if (rdb instanceof RDRange)// nested Cell
                    {
                        RDRange rdr = (RDRange) rdb;
                        cd = ((NestedBox) ((TemplateModel) rdr.getModel()).getFrame()).getCell();
                    } else if (rdb instanceof CellDef)
                        cd = (CellDef) rdb;
                    else if (rdb instanceof RDRepeaterNext)
                        continue;
                    else
                        throw new JoriaAssertionError("Unhandled Run Data Type" + rdb.getClass());
                    FlexSize cfs = myModel.getColSizingAt(wPos - myModel.getStartCol());
                    boolean isFlex = false;
                    if (cd.getCascadedStyle().getSpanHorizontal() > 1) {
                        int hspan = cd.getCascadedStyle().getSpanHorizontal();
                        for (int i = 0; i < hspan; i++) {
                            cfs = myModel.getColSizingAt(wPos - myModel.getStartCol() + i);
                            if (cfs.getUnit() == FlexSize.flex) {
                                isFlex = true;
                                break;
                            }
                        }
                    } else
                        isFlex = cfs.getUnit() == FlexSize.flex;
                    float bw = getHorizontalBorderWidth(r, c);
                    CellStyle cs = cd.getCascadedStyle();
                    float pictureWidth = bw;
                    if (cs.getBackgroundImageName() != null) {
                        if (cs.getBackgroundImageTargetWidth() != null && !cs.getBackgroundImageTargetWidth().isExpandable())
                            pictureWidth += cs.getBackgroundImageTargetWidth().getVal();
                        else
                            pictureWidth += cs.getBackgroundImage(loc).getIconWidth();
                    }
                    bw += cs.getLeftRightPaddingValue();
                    if (isFlex) {
                        final float old = widths[r + rBase][wPos];
                        final float cdw;
                        if (rdb instanceof RDRange)// nested Cell
                        {
                            RDRange rdr = (RDRange) rdb;
                            cdw = rdr.getMaxWidth(vv, loc, g, (NestingCellDef) cd);
                        } else if (rdb instanceof CellDef)
                            cdw = cd.getMaxWidth(vv, loc, g);
                        else
                            throw new JoriaAssertionError("Unhandled Run Data Type");
                        final float newWidth = cdw + bw;
                        widths[r + rBase][wPos] = Math.max(old, Math.max(newWidth, pictureWidth));
                    } else {
                        if (vv instanceof RVStringCol)// force conversion of int, boolean, float columns to strings
                            ((RVStringCol) vv).buildFormattedStrings(cd, loc);
                        widths[r + rBase][wPos] = cfs.getVal() + bw;
                    }
                    wPos++;
                }
                // else RDRepeaterNext do nothing: width already calculated
            }
        }
    }

    protected float getHorizontalBorderWidth(int r, int c) {
        return myModel.getBorderAt(r, c, false, true).getThickness() / (myModel.isFirstCol(c) ? 1 : 2) + myModel.getBorderAt(r, c, false, false).getThickness() / (myModel.isLastCol(c) ? 1 : 2);
    }

    public void format(RVAny values, Locale loc) {
        RVTemplate v = (RVTemplate) values;
        if (v == null)
            return;
        for (int r = 0; r < fields.length; r++) {
            RDBase[] row = fields[r];
            for (int c = 0; c < row.length; c++) {
                RDBase rdb = row[c];
                if (rdb == null) {
                    continue;
                }
                RVAny vv = v.get(r, c);
                if (rdb instanceof RDRepeater) {
                    RDRepeater rdr = (RDRepeater) rdb;
                    if (vv instanceof RVObjects) {
                        RVObjects vvo = (RVObjects) vv;
                        for (int vvox = 0; vvox < vvo.elementCount(); vvox++) {
                            rdr.format(vvo.elems[vvox], loc);
                        }
                    } else
                        rdr.format(vv, loc);
                } else if (rdb instanceof CellDef) {
                    CellDef cd = (CellDef) rdb;
                    if (vv instanceof RVStringCol) {
                        RVStringCol rvStringCol = (RVStringCol) vv;
                        rvStringCol.buildFormattedStrings(cd, loc);
                    }
                }
                // else RDRepeaterNext do nothing: already formatted
            }
        }
    }
}
