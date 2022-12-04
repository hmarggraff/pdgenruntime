// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.styledtext.model.StyledParagraphList;
import org.pdgen.data.view.AggregateDef;

import java.util.ArrayList;

/**
 * User: patrick
 * Date: Aug 14, 2006
 * Time: 7:28:42 AM
 */
public class RVStyledText implements RValue
{
    StyledParagraphList text;

    public RVStyledText(StyledParagraphList text)
    {
        this.text = text;
    }

    public String get(int at)
    {
		return text.getAsString();
   }

    public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter)
    {
        collector.accumulate(aggregates,  get(iter));
    }

    public StyledParagraphList getText()
    {
        return text;
    }
}
