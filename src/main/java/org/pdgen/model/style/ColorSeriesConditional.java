// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.JoriaType;
import org.pdgen.data.VariableProvider;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLParser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class ColorSeriesConditional implements ColorSeries, VariableProvider
{
    private static final long serialVersionUID = 7L;
    final String[] conditions;
	final Color[] colors; // one more colors than condititions (default color)
	final JoriaType scope;

	public ColorSeriesConditional(String[] conditions, Color[] colors, JoriaType scope)
	{
		this.conditions = conditions;
		this.colors = colors;
		this.scope = scope;
	}

	public Color getColorAt(DBData val, RunEnv env) throws JoriaDataException
	{
		for (int ix = 0; ix < conditions.length - 1; ix++)
		{
			JoriaQuery q = getParsedCondition(ix);
			final boolean bool = q.getBooleanValue(env, val);
			if (bool)
				return colors[ix];
		}
		return colors[colors.length - 1];
	}

	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		final Rectangle bounds = g2.getClipBounds();
		final float width = bounds.width;
		final int slices = colors.length;
		float step = width / slices;
		float at = 0;
		final int height = bounds.height;
		for (Color color : colors) {
			g2.setColor(color);
			g2.fill(new Rectangle2D.Float(at, 0, step, height));
			at += step;
		}
	}

	public Color getColorAt(int i, Color[] computedColors, int count)
	{
		if (computedColors != null)
			return computedColors[i];
		else
		{
			i = Math.min(i, colors.length - 1);
			return colors[i];
		}
	}

	public Color getDefaultColor()
	{
		return colors[colors.length - 1];
	}

	public JoriaQuery getParsedCondition(int i)
	{
		JoriaQuery q = OQLParser.lookInCache(conditions[i], scope);
		if (q != null)
			return q;
		try
		{
			q = OQLParser.parse(conditions[i], scope, false);
		}
		catch (Exception ex)
		{
			throw new JoriaInternalError("Unexpected Parse Exception in run", ex);
		}
		return q;
	}

	public String[] getConditions()
	{
		return conditions;
	}

	public Color[] getColors()
	{
		return colors;
	}

	public void collectVariables(Set<RuntimeParameter> v, Set<Object> seen)
	{
		//Todo implement in Colorseriesconditional collectVariables
	}

	public void collectI18nKeys2(HashMap<String,List<I18nKeyHolder>> s, Set<Object> seen)
	{
		//Todo implement in Colorseriesconditional collectI18nKeys2
	}

	public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen)
	{
		//Todo implement in Colorseriesconditional collectVisiblePickersInScope
	}
}
