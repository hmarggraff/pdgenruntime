// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.NestingCellDef;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.NestedBox;

import java.util.Stack;
import java.util.Locale;
import java.awt.*;

public class RDRange extends RDRangeBase
{

    private static final long serialVersionUID = 7L;

    public RDRange(TemplateModel model, RDBase[][] fields)
    {
        super(model, fields);
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
    {

	    if (!isVisible(outMode, from))
	        return null;
        RVAny[][] fill = new RVAny[myModel.getRowCount()][];
        RVTemplate ret = new RVTemplate(fill);
        ret.setElementCount(1);
        defs.push(this);
        outerVals.push(ret);
        for (int r = 0; r < fill.length; r++)
        {
            RDBase[] rDef = fields[r];
            RVAny[] row = new RVAny[rDef.length];
            fill[r] = row;
            for (int c = 0; c < row.length; c++)
            {
                RDBase rdb = rDef[c];
                if (rdb == null)
                    continue;

                row[c] = rdb.buildRunValue(from, outMode, defs, outerVals, g);
            }
        }
        defs.pop();
        outerVals.pop();
        return ret;
    }

	protected boolean isVisible(final OutputMode outMode, final DBData from) throws JoriaDataException
	{
		if (!(myModel instanceof TemplateModel))
			return true;
		TemplateModel m = (TemplateModel) myModel;
		if (!(m.getFrame() instanceof NestedBox))
			return true;
		NestedBox nb = (NestedBox) m.getFrame();
		return nb.isVisible(outMode.getRunEnv(), from) && nb.getCell().isVisible(outMode, from);
	}

	public float getMaxWidth(final RVAny vv, final Locale loc, final Graphics2D g, NestingCellDef ncd)
	{
		float[][] cells = new float[ncd.getInnerBox().getTemplate().getRowCount()][];
		final int colCount = ncd.getInnerBox().getTemplate().getColCount();
		for (int i = 0; i < cells.length; i++)
		{
			cells[i] = new float[colCount];

		}
		calcMaxWidth(cells, vv, loc, g);
		float[] colWidths = new float[colCount];
		for (float[] row : cells)
		{
			for (int j = 0; j < row.length; j++)
			{
				float v = row[j];
				colWidths[j] = Math.max(v, colWidths[j]);
			}
		}
		float ret = 0;
		for (float w : colWidths)
		{
			ret += w;
		}
		return ret;
	}
}
