// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.awt.*;

public class ColorSeriesSingle implements ColorSeries
{
    private static final long serialVersionUID = 7L;
    final Color myColor;

	public ColorSeriesSingle(Color color)
	{
		myColor = color;
	}

	public Color getColorAt()
	{
		return myColor;
	}

	public Color getColor()
	{
		return myColor;
	}

	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		final Rectangle bounds = g2.getClipBounds();
		g2.setColor(myColor);
		g2.fill(bounds);
	}

	public Color getColorAt(int i, Color[] colors, int count)
	{
		return myColor;
	}

	public Color getDefaultColor()
	{
		return myColor;
	}
}
