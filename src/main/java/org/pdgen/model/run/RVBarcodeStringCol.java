// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.Barcode4jCell;

public class RVBarcodeStringCol extends RVStringCol
{
	Barcode4jCell src;

	public RVBarcodeStringCol(final String[] strings, final Barcode4jCell src)
	{
		super(strings);
		this.src = src;
	}

	public RVBarcodeStringCol(final Barcode4jCell src)
	{
		this.src = src;
	}

	public RVBarcodeStringCol(final int size, final Barcode4jCell src)
	{
		super(size);
		this.src = src;
	}
}
