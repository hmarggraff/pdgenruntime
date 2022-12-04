// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.SortedNamedVector;
import org.pdgen.env.Env;

/**
 * User: hmf at Oct 14, 2004 12:53:57 PM
 * Class is used as a marker
 */
public class ChartStyle extends BoxStyle
{
	public static final String defaultChartStyleName = "*DefaultChartStyle";
    private static final long serialVersionUID = 7L;

    public ChartStyle(String aName)
	{
		super(aName);
	}


	protected void storeSpecialStyle()
	{
	}

    @Override
    public SortedNamedVector getGlobalStyleList() {
        return Env.instance().repo().chartStyles;
    }
}
