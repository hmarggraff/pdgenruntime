// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.env.Env;
import org.pdgen.model.*;
import org.pdgen.model.run.RVAny;
import org.pdgen.model.style.FrameStyle;
import org.pdgen.oql.OQLParseException;

import java.awt.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class NestingCellDef extends FreeCellDef // TODO accesses, rebind u.s.w.
{
    private static final long serialVersionUID = 7L;
    protected NestedBox innerBox;

    public NestingCellDef(TemplateModel parentGrid) {
        super(parentGrid);
        innerBox = new NestedBox(this);
    }

    public NestingCellDef(NestingCellDef from, TemplateModel parentGrid) {
        super(from, parentGrid);
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences) {
        NestingCellDef ret = new NestingCellDef(this, newContainerGrid);
        ret.innerBox = innerBox.duplicate(ret, copiedReferences);
        return ret;
    }

    public void collectI18nKeys(HashSet<String> keySet) {
        innerBox.getTemplate().collectI18nKeys(keySet);
    }

    public int getColPosition() {
        for (int r = 0; r < grid.getRowCount(); r++) {
            for (int c = 0; c < grid.getColCount(); c++) {
                final CellDef cellDef = grid.cellAt(r, c);
                if (cellDef == this)
                    return c;
            }
        }
        throw new JoriaAssertionError("Cell not in this grid");
    }

    public NestedBox getInnerBox() {
        return innerBox;
    }

    public int getRowPosition() {
        for (int r = 0; r < grid.getRowCount(); r++) {
            for (int c = 0; c < grid.getColCount(); c++) {
                final CellDef cellDef = grid.cellAt(r, c);
                if (cellDef == this)
                    return r;
            }
        }
        throw new JoriaAssertionError("Cell not in this grid");
    }

    public void setInnerBox(NestedBox newInnerBox) {
        innerBox = newInnerBox;
    }

    public void fixAccess() {
        innerBox.getTemplate().fixAccess();
    }

    public void reloadStyles() {
        FrameStyle fs = innerBox.getFrameStyle();
        if (fs != null && fs.getName() != null)
            innerBox.setFrameStyle(Env.instance().repo().frameStyles.find(fs.getName()));
        innerBox.getTemplate().reloadStyles();
    }

    public void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException {
        super.getUsedAccessors(s);
        innerBox.getUsedAccessors(s);
    }

    public RDBase getColumnDefs() {
        return innerBox.getTemplate().getColumnDefs();
    }

    public float getMaxWidth(RVAny values, Locale loc, Graphics2D g) {
        return super.getMaxWidth(values, loc, g);
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
/*
		Rectangle2D.Float frameIndicator = new java.awt.geom.Rectangle2D.Float(x0,y0, w-1,h-1);
		Color oldColor = p.getColor();
		p.setColor(Color.red);
		p.draw(frameIndicator);
		p.setColor(oldColor);
*/
        //p.translate(8,8);
        getInnerBox().getTemplate().getLayouter().paintIt(p);
        //p.translate(-8,-8);
    }

    protected void adjustSize(Locale loc, Graphics2D g) {
        super.adjustSize(loc, g);
        TemplateLayouter innerLayouter = getInnerBox().getTemplate().getLayouter();
        float h = innerLayouter.getHeight();
        myWidth += innerLayouter.getActualWidth();
        myHeight += h;

    }

    public Repeater getContainingRepeater() {
        if (repeater != null)
            return repeater;
        if (grid.getFrame() instanceof NestedBox) //nested: check if outer level is in a repeater
        {
            NestedBox nestedBox = (NestedBox) grid.getFrame();
            return nestedBox.getCell().getContainingRepeater();
        }
        return null; // not nested
    }
}
