// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.util.BucketList;
import org.pdgen.model.Template;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Vector;

public class DisplayList extends BucketList<GraphicElement>
{
	private Vector<GrahElPostprocess> totalPages;
	float pageHeightBase;
	float pageWidthBase;
	Template currentTemplate;
    String pageNumberText;

    public DisplayList()
	{
	}

	public DisplayList(float pageWidthBase, float pageHeightBase, Template template)
	{
		this.pageHeightBase = pageHeightBase;
		this.pageWidthBase = pageWidthBase;
		currentTemplate = template;
	}

	public void add(GraphicElement e)
	{
        if(e instanceof GraphElLine)
        {
            BLIterator iterator = getIterator();
            while(iterator.more())
            {
                GraphicElement element = iterator.next();
                if(element instanceof GraphElLine)
                {
                    GraphElLine line = (GraphElLine) element;
                    if(line.extend((GraphElLine) e))
                        return;
                }
            }
        }
        addImpl(e);
	}

    public void translate(float offsetx, float offsety)
    {
        BLIterator it = new BLIterator();
        while(it.more())
        {
            GraphicElement elem = it.next();
            elem.translate(offsetx, offsety);
        }
    }

    public GraphicElement get(int ix)
	{
		return getImpl(ix);
	}

	public BLIterator getIterator()
	{
		return new BLIterator();
	}

	public void dump()
	{
		try
		{
			PrintStream ps = new PrintStream(new FileOutputStream("displaylist.txt"));
			BLIterator it = getIterator();
			while (it.more())
			{
				GraphicElement ge = it.next();
				ge.dump(ps);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}
	}

	public void add(DisplayList r)
	{
		super.add(r);
		Vector<GrahElPostprocess> pages = r.totalPages;
		if (pages != null)
		{
			if (totalPages == null)
				totalPages = new Vector<GrahElPostprocess>();
			totalPages.addAll(pages);
		}
	}

	public void truncate(int newSize)
	{
		super.truncate(newSize);
		if (totalPages != null)
		{
			int keep = 0;
            for (GrahElPostprocess totalPage : totalPages)
            {
                if (totalPage.getPosInDisplayList() < newSize)
                    keep++;
            }
			if (keep == 0)
				totalPages = null;
			else
				totalPages.setSize(keep);
		}
	}

	public void addTotalPages(GrahElPostprocess grel)
	{
		if (totalPages == null)
			totalPages = new Vector<GrahElPostprocess>(1);
		grel.setPosInDisplayList(size);
		totalPages.add(grel);
	}

	public Vector<GrahElPostprocess> getTotalPages()
	{
		return totalPages;
	}

	public class BLIterator
	{
		int bucket;
		int bPos;

		public GraphicElement next()
		{
			if (bPos >= BucketList.bucketSize)
			{
				if (more())
				{
					bPos = 0;
					bucket++;
				}
				else
				{
					throw new NoSuchElementException("No next element after " + size);
				}
			}
			return (GraphicElement) data[bucket][bPos++];
		}

		public boolean more()
		{
			return bPos + bucket * BucketList.bucketSize < size;
		}
	}

    public boolean contains(GraphicElement elem)
    {
        BLIterator it = getIterator();
        while (it.more())
        {
            GraphicElement graphicElement =  it.next();
            if(elem.equals(graphicElement))
                return true;
        }
        return false;
    }
}
