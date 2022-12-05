// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.JoriaAccess;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.FlexSize;
import org.pdgen.model.style.JoriaSimpleBorder;

public interface ModelBase {

    CellDef cellAt(int r, int c);

    int getColCount();

    Repeater getRepeaterAt(int sRow, int sCol);

    RepeaterList getRepeaterList();

    int getRepeaterCount();

    int getRowCount();

    JoriaAccess getAccessor();

    int getStartRow();

    int getStartCol();

    int getEndRow();

    int getEndCol();

    FlexSize getColSizingAt(int at);

    FlexSize getRowSizingAt(int at);

    RDCrossTab getCrosstab();

    JoriaSimpleBorder getBorderAt(int r, int c, boolean horizontal, boolean lefttop_vs_rightbottom);

    boolean isFirstRow(int r);

    boolean isLastRow(int r);

    boolean isFirstCol(int c);

    boolean isLastCol(int c);

    TemplateBoxInterface getFrame();
}
