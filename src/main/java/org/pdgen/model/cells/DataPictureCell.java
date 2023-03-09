// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Res;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.*;
import org.pdgen.oql.OQLParseException;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * User: patrick
 * Date: Nov 23, 2004
 */
public class DataPictureCell extends PictureCellBase implements DataCell {
    private static final long serialVersionUID = 7L;
    protected JoriaAccess data;
    protected String myText;

    public void makeName() {
        myText = abbrev(data.getName());
        myWidth = Float.NaN;
        myHeight = Float.NaN;
        grid.fireChange("DataCellDef displayName changed");
    }

    public static String abbrev(String name) {
        if (name.length() > 11)
            return name.substring(0, 5) + '\'' + name.substring(name.length() - 5);
        return name;
    }

    public DataPictureCell(TemplateModel containerGrid, JoriaAccess dataAxs) {
        super(containerGrid);
        data = dataAxs;
    }

    public DataPictureCell(FreeCellDef from, TemplateModel containerGrid) {
        super(from, containerGrid);
        final DataPictureCell pictureCell = (DataPictureCell) from;
        data = pictureCell.data;
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences) {
        return new DataPictureCell(this, newContainerGrid);
    }

    protected void adjustSize(Locale loc, Graphics2D g) {
        super.adjustSize(loc, g);
        if (getClass() == DataPictureCell.class) {
            if (targetWidth == null || targetWidth.isExpandable()) {
                myWidth += 8;
                myHeight += 5;
            } else {
                myWidth += targetWidth.getVal();
                myHeight += 5;
            }
        }
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        p.setColor(getCascadedStyle().getForeground());
        p.draw(new Ellipse2D.Float(x0 + 1, y0 + 1, w - 2, h - 2));
        String t = myText == null ? "image" : myText;
        p.drawString(t, x0 + w / 2, y0 + h / 2);
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        if (repeater != null) {
            DBCollection source = (DBCollection) from;
            if (source == null)
                return null;
            final int length = Math.max(source.getLength(), RVStringCol.startSize);
            return new RVImageCol(length);
        } else {
            if (from != null && data != null) {
                if (!isVisible(outMode, from))
                    return RVSupressHeader.instance;
                try {
                    Object pict;
                    if (data.isAccessTyped()) {
                        pict = ((JoriaAccessTyped) data).getPictureValue((DBObject) from, outMode.getRunEnv());
                    } else {
                        final DBData d = data.getValue(from, data, outMode.getRunEnv());
                        if (d == null || d.isNull())
                            return null;
                        if (!(d instanceof DBString) && !(d instanceof DBImage))
                            throw new JoriaAssertionError("only string or image typed queries can be images");
                        if (d instanceof DBString) {
                            DBString ds = (DBString) d;
                            pict = ds.getStringValue();
                        } else {
                            pict = ((DBImage) d).getData();
                        }
                    }
                    if (pict == null)
                        return null;
                    return new RVImage(pict, outMode.getRunEnv().getLocale(), false);
                } catch (JoriaDataRetrievalExceptionInUserMethod e) {
                    return new RVImage(Res.missingImageIcon, outMode.getRunEnv().getLocale(), false);
                }
            }
        }
        return null;
    }

    public float getMaxWidth(RVAny value, Locale loc, Graphics2D g) {
        if (value == null || value == RVSupressHeader.instance)
            return 0;
        if (targetWidth != null && !targetWidth.isExpandable())
            return targetWidth.getVal();
        else if (value instanceof RVImageCol) {
            RVImageCol ic = (RVImageCol) value;
            float width = 0;
            for (int i = 0; i < ic.getSize(); i++) {
                if (ic.getIcon(i) != null)
                    width = Math.max(width, ic.getIcon(i).getIconWidth());
            }
            return width;
        } else if (value instanceof RVImage) {
            return ((RVImage) value).getPicture().getIconWidth();
        } else
            throw new JoriaAssertionError("Unhandled data value: " + value.getClass());
    }

    public JoriaAccess getAccessor() {
        return data;
    }

    public String getDisplayMode() {
        return "Some Picture";
    }

    public void makeUnboundCell() {
        data = null;
    }

    public void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException {
        super.getUsedAccessors(s);
        s.add(data);
    }

    public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException {
        if (data != null)
            return out.makePictureGraphEl(tblReq, iter);
        else
            return out.makeEmptyGrel(tblReq);
    }

    public void rebindByName(final JoriaClass newScope) {
        final String pName = data.getName();
        if (newScope != null) {
            data = newScope.findMemberIncludingSuperclass(pName);
            myText = data.getName();
        } else {
            myText = "?";
            data = null;
        }
    }

    public void collectVariables(Set<RuntimeParameter> v, Set<Object> seen) {
        if (data instanceof VariableProvider)
            ((VariableProvider) data).collectVariables(v, seen);
    }

    public boolean visitAccesses(AccessVisitor visitor, Set<JoriaAccess> seen) {
        if (!visitor.visit(data))
            return false;
        if (data instanceof VisitableAccess) {
            return ((VisitableAccess) data).visitAllAccesses(visitor, seen);
        }
        return true;
    }
}
