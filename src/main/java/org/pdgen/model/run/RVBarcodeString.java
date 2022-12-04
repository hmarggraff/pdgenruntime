// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.Barcode4jCell;

public class RVBarcodeString extends RVString
{
	Barcode4jCell src;

	public RVBarcodeString(final String string, float width, final Barcode4jCell src)
	{
		super(string, width);
		this.src = src;
	}
}
