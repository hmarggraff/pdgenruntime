// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

public interface ColorSeries extends Serializable
{
	void paintComponent(Graphics g);

	Color getColorAt(int i, Color[] colors, int count);

	Color getDefaultColor();
}
