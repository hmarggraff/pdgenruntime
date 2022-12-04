// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.StyledTextCellDef;
import org.pdgen.styledtext.model.StyledParagraphList;
import org.pdgen.data.DBObject;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.JoriaDataRetrievalExceptionInUserMethod;
import org.pdgen.data.Trace;
import org.pdgen.data.view.AggregateDef;

import java.util.ArrayList;

/**
 * User: patrick
 * Date: Aug 14, 2006
 * Time: 7:39:08 AM
 */
public class RVStyledTextCol implements RValue, RVCol
{
    StyledParagraphList[] texts;
    public static final int startSize = 4;
    protected static final int endExpSize = 0x10000;

    public RVStyledTextCol(int length)
    {
        texts = new StyledParagraphList[length];
    }
    void checkBuffer(int at)
    {
        if (texts == null || at >= texts.length) // kein Buffer oder zuklein
        {
            int oldSize = (texts == null ? 0 : texts.length);
            int newSize = calculateNewBufferSize(oldSize, at + 1);
            StyledParagraphList[] newArray = new StyledParagraphList[newSize];
            if (texts != null)
            {
                System.arraycopy(texts, 0, newArray, 0, oldSize);
            }
            texts = newArray;
        }
    }

    protected int calculateNewBufferSize(int oldSize, int needed)
    {
        if (needed <= startSize)
        {
            return startSize;
        }
        else if (needed > endExpSize)
        {
            return ((needed / endExpSize) + 1) * endExpSize;
        }
        else
        {
            int size = oldSize;
            do
            {
                size *= 2;
            }
            while (size < needed);
            return size;
        }
    }


    public String get(int at)
    {
        Trace.check(texts);
        if (texts.length > 0)
        {
            StyledParagraphList text = texts[at];
            return text.getAsString();
        }
        else
            return null;
    }

    public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter)
    {
        collector.accumulate(aggregates, get(iter));
    }

    public void add(int at, DBObject o, CellDef cd, OutputMode env) throws JoriaDataException
    {
	    StyledTextCellDef scd = (StyledTextCellDef) cd;
        checkBuffer(at);
        try
        {
            texts[at] = scd.getStyledText(o, env);
        }
        catch (JoriaDataRetrievalExceptionInUserMethod e)
        {

            texts[at] = new StyledParagraphList(JoriaAccess.ACCESSERROR);
        }

    }

    public StyledParagraphList getText(int iter)
    {
        return texts[iter];
    }

    public int getSize()
    {
        return texts.length;
    }
}
