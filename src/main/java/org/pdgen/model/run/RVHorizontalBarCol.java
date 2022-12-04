// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.HorizontalBarCell;
import org.pdgen.data.DBObject;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.JoriaDataRetrievalExceptionInUserMethod;
import org.pdgen.env.Res;

import javax.swing.*;
import java.awt.*;

public class RVHorizontalBarCol extends RVImageCol
{
    double[] data;
    boolean converted;
    HorizontalBarCell hbcd;

    public RVHorizontalBarCol(int size, HorizontalBarCell hbcd)
    {
        super(size);
        this.hbcd = hbcd;
        converted = !hbcd.isPercentage();
        if(!converted)
            data = new double[size];
    }

    void checkBuffer(int at)
    {
        super.checkBuffer(at);
        if(!converted && images.length != data.length)
        {
            double [] newData = new double[images.length];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    public void add(int at, DBObject o, CellDef rcd, OutputMode env) throws JoriaDataException
    {
        checkBuffer(at);
        if(!converted)
        {
            data[at] = hbcd.getValueDouble(o, env.getRunEnv().pager);
        }
        else
        {
            try
            {
	            if (hbcd.getAccessor() == null)
	            {
		            images[at] = null;
		            return;

	            }
                double val = hbcd.getValueDouble(o, env.getRunEnv().pager);
                Image image = hbcd.makeImage(val);
                if(image != null)
                {

                    images[at] = new ImageIcon(image);
                }
                else
                {
                    images[at] = null;
                }
                storedData[at] = null;
            }
            catch (JoriaDataRetrievalExceptionInUserMethod e)
            {
                images[at] = new ImageIcon(Res.class.getResource("pix/pdfmiss.png"));
            }
        }
    }

    private void convert()
    {
        double sum = 0;
        double max = -Double.MAX_VALUE;
        for (double val : data)
        {
            if (!Double.isNaN(val))
                sum += val;
            max = Math.max(max, val);
        }
        for (int i = 0; i < data.length; i++)
        {
            if (Double.isNaN(data[i]) || sum == 0)
                images[i] = null;
            else
            {
                double number;
                if(hbcd.isPercentageSum())
                    number = data[i] / sum;
                else
                    number = data[i] / max;
                Image image = hbcd.makeImage(number);
                if(image != null)
                {

                    images[i] = new ImageIcon(image);
                }
                else
                {
                    images[i] = null;
                }
            }
            storedData[i] = null;
        }
        converted = true;
    }

    public Object getInformation(int i)
    {
        if(!converted)
            convert();
        return super.getInformation(i);
    }

    public Icon getIcon(int i)
    {
        if(!converted)
            convert();
        return super.getIcon(i);
    }

    public String get(int at)
    {
        if(!converted)
            convert();
        return super.get(at);
    }


    public boolean doSpread()
    {
        return true;
    }
}
