// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.pdf;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Nov 16, 2004
 * Time: 10:26:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class PdfGState
{

    public Float fillOpacity;
    public Float strokeOpacity;
    public byte[] outName;

    public PdfGState(byte[] on)
    {
        outName = on;
    }

    public void setFillOpacity(float v)
    {
        fillOpacity = v;
    }

    public void setStrokeOpacity(float v)
    {
        strokeOpacity = v;
    }
}
