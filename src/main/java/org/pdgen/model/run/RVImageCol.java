// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.JoriaImage;
import org.pdgen.env.Res;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataPictureCell;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class RVImageCol implements RValue, RVImageBase, RVCol {
    Object[] storedData; // either String or byte[]
    Icon[] images;
    public static final int startSize = 4;
    protected static final int endExpSize = 0x10000;

    public RVImageCol(int size) {
        storedData = new Object[size];
        images = new Icon[size];
    }

    void checkBuffer(int at) {
        if (storedData == null || at >= storedData.length) // kein Buffer oder zuklein
        {
            int oldSize = (storedData == null ? 0 : storedData.length);
            int newSize = calculateNewBufferSize(oldSize, at + 1);
            Object[] newStoredData = new Object[newSize];
            Icon[] newImages = new Icon[newSize];
            if (storedData != null) {
                System.arraycopy(storedData, 0, newStoredData, 0, oldSize);
                System.arraycopy(images, 0, newImages, 0, oldSize);
            }
            storedData = newStoredData;
            images = newImages;
        }
    }

    protected int calculateNewBufferSize(int oldSize, int needed) {
        if (needed <= startSize) {
            return startSize;
        } else if (needed > endExpSize) {
            return ((needed / endExpSize) + 1) * endExpSize;
        } else {
            int size = oldSize;
            do {
                size *= 2;
            }
            while (size < needed);
            return size;
        }
    }

    public void add(int at, DBObject o, CellDef cd, OutputMode env) throws JoriaDataException {
        checkBuffer(at);
        try {
            DataPictureCell rcd = (DataPictureCell) cd;
            JoriaAccess axs = rcd.getAccessor();
            Object pict;
            if (axs.isAccessTyped()) {
                pict = ((JoriaAccessTyped) axs).getPictureValue(o, env.getRunEnv());

            } else {
                final DBImage dbImage = ((DBImage) axs.getValue(o, axs, env.getRunEnv()));
                if (dbImage != null && !dbImage.isNull())
                    pict = dbImage.getData();
                else
                    pict = null;
            }

            Object storedData = null;
            Locale loc = env.getRunEnv().getLocale();
            if (pict instanceof JoriaImage)
                storedData = ((JoriaImage) pict).getRawImageData();
            Icon icon;
            if (pict instanceof String) {
                storedData = Internationalisation.localizeFileName((String) pict, loc);
                icon = RVImage.buildImageFromFileName((String) pict, loc, true);
            } else if (pict instanceof byte[]) {

                storedData = pict;
                icon = RVImage.buildImageFromBytes((byte[]) pict);
            } else if (pict instanceof Image) {
                icon = new ImageIcon((Image) pict);
            } else if (pict instanceof Icon) {
                icon = (Icon) pict;
            } else if (pict == null) {
                icon = null;
            } else if (storedData != null) {
                icon = RVImage.buildImageFromBytes((byte[]) storedData);
            } else
                throw new JoriaAssertionError("unsupported picture class type " + pict.getClass().getName());

            images[at] = icon;
            this.storedData[at] = storedData;
        } catch (JoriaDataRetrievalExceptionInUserMethod | IOException e) {
            Trace.logError("Image not read: " + e.getMessage());
            images[at] = new ImageIcon(Res.class.getResource("pix/pdfmiss.png"));
        }
    }

    public int getSize() {
        return storedData.length;
    }

    public String get(int at) {
        return "No Strings in pictures";
    }

    public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter) {
        // No accumulate in pictures
    }

    public Icon getIcon(int i) {
        return images[i];
    }

    public Object getInformation(int i) {
        return storedData[i];
    }

    public boolean doSpread() {
        return false;
    }

    public double getHardScale() {
        return 0;
    }
}
