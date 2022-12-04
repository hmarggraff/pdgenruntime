// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.Internationalisation;
import org.pdgen.data.Trace;
import org.pdgen.model.run.GraphElRtfText;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;
import java.util.Locale;

/**
 * User: patrick
 * Date: Feb 28, 2005
 * Time: 7:36:24 PM
 */
public class RtfLayouter implements TextCellLayouter
{
    View hv1;
    SimpleTextCellDef myCell;
    static ThreadData tc = new ThreadData();

	public RtfLayouter(SimpleTextCellDef cell)
	{
		myCell = cell;
 	}
    public void paint(Graphics2D g, float x0, float y0, float w, float h, Locale loc)
    {
        Trace.logDebug(Trace.layout, "hPaint x" + x0 + "y "+ y0 + "w " + w + " h " + h);
        if(hv1 == null)
            calcSize(Internationalisation.NOREPLACE, g);
        Rectangle r = new Rectangle(Math.round(x0), Math.round(y0), Math.round(w), Math.round(h));
        hv1.paint(g, r);
    }

    public static boolean checkTextCompatible(String toCheck)
    {
        try
        {
            GraphElRtfText.buildView(toCheck, getTc());
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public void calcSize(Locale loc, Graphics2D g)
    {
        String myTxt = myCell.getWrappedText(loc).trim();
        getTc().setBackground(myCell.getCascadedStyle().getBackground());
        getTc().setFont(myCell.getCascadedStyle().getStyledFont());
        hv1 = GraphElRtfText.buildView(myTxt, getTc());
        myCell.myWidth = hv1.getPreferredSpan(View.X_AXIS);
        myCell.myHeight = hv1.getPreferredSpan(View.Y_AXIS);
        Trace.logDebug(Trace.layout, "RTFsize w " + myCell.myWidth + " h " + myCell.myWidth);
    }

    static class ThreadData extends ThreadLocal
    {

        protected Object initialValue()
        {
            return new JPanel();
        }
    }
    public static JComponent getTc()
    {

        return (JComponent) tc.get();
    }
}
