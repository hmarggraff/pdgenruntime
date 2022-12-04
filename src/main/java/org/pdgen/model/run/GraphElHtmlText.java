// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.env.Env;

import javax.swing.*;
import javax.swing.text.View;
import java.util.Map;

/**
 * User: patrick
 * Date: Nov 18, 2004
 * Time: 2:08:29 PM
 */
public class GraphElHtmlText extends GraphElContent
{
    private static final long serialVersionUID = 7L;
    public String myContent;
	public CellStyle myStyle;
	public transient View hv;

    private GraphElHtmlText(GraphElHtmlText from)
    {
        super(from);
        myContent = from.myContent;
        myStyle = from.myStyle;
        hv = from.hv;

    }
    public GraphElHtmlText(CellStyle style, String content, View hv1, CellDef src, ImageIcon backgroundImage)
	{
		super(style.getBackground(), src, backgroundImage);
		myContent = content;
		myStyle = style;
		hv = hv1;
	}

	public void print(JoriaPrinter pr)
	{
		pr.printGEHtmlText(this);
	}

	/*
		We need a swing component to render html text
	 */
	public static JComponent tc()
	{
		Map<Object, Object> threadLocal = Env.instance().getThreadLocalStorage().getMap();
		JComponent comp = (JComponent) threadLocal.get(GraphElHtmlText.class);
		if (comp == null)
		{
			comp = new JPanel();
			threadLocal.put(GraphElHtmlText.class, comp);
		}
		return comp;
	}

    public GraphElContent copy()
    {
        return new GraphElHtmlText(this);
    }
}
