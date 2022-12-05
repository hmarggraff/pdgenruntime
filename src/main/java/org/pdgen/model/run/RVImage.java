// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.Internationalisation;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.Trace;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaImage;
import org.pdgen.env.Res;
import org.pdgen.util.BMPReader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageProducer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class RVImage implements RValue, RVImageBase {

    String pictureFileName;
    byte[] pictureData;
    protected transient Icon myPicture;
    boolean spread;

    public RVImage(String pictureFileName, ImageIcon picture) {
        this.pictureFileName = pictureFileName;
        myPicture = picture;
    }

    public RVImage(Object pict, Locale loc, boolean spread) {
        this.spread = spread;
        if (pict instanceof JoriaImage)
            pictureData = ((JoriaImage) pict).getRawImageData();
        if (pict instanceof String) {
            pictureFileName = Internationalisation.localizeFileName((String) pict, loc);
            myPicture = buildImageFromFileName((String) pict, loc, true);
        } else if (pict instanceof byte[]) {
            pictureData = (byte[]) pict;
            myPicture = new ImageIcon(pictureData);
        } else if (pict instanceof Image) {
            myPicture = new ImageIcon((Image) pict);
        } else if (pict instanceof Icon) {
            myPicture = (Icon) pict;
        } else if (pict == null) {
            myPicture = new ImageIcon(Res.class.getResource("pix/pdfmiss.png"));
        } else if (pictureData != null) {
            myPicture = new ImageIcon(pictureData);
        } else
            throw new JoriaAssertionError("unsupported picture class type " + pict.getClass().getName());
    }


    protected RVImage() {
    }

    public String get(int at) {
        return pictureFileName;
    }

    public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter) {
    }

    public String getPictureFileName() {
        return pictureFileName;
    }

    public Icon getPicture() {
        return myPicture;
    }

    public byte[] getPictureData() {
        return pictureData;
    }

    public static ImageIcon buildImageFromFileName(String pictureFileName, Locale loc, boolean complain) {
        String pfn = Internationalisation.localizeFileName(pictureFileName, loc);
        try {
            if (Env.instance().getFileService().existsAsFile(pfn)) {
                final URL fileAsURL = Env.instance().getFileService().getFileAsURL(pfn);
                return new ImageIcon(fileAsURL, fileAsURL.toString());
            } else {
                if (complain)
                    Trace.logError("Picture file not found: " + pfn + " --> " + Env.instance().getFileService().getFileName(pfn));
                return Res.missingImageIcon;
            }
        } catch (IOException e) {
            if (complain)
                Trace.logError("Picture file unreadable: " + pfn);
            return Res.missingImageIcon;
        }
    }

    public static Icon buildImageFromBytes(byte[] imageData) throws IOException {
        Icon icon;
        if (imageData[0] == 'B' && imageData[1] == 'M') {
            ImageProducer imageP = BMPReader.getBMPImage(new ByteArrayInputStream(imageData));
            Image image = Toolkit.getDefaultToolkit().createImage(imageP);
            icon = new ImageIcon(image);
        } else {
            icon = new ImageIcon(imageData);
        }
        return icon;
    }

    public Icon getIcon(int i) {
        return myPicture;
    }

    public Object getInformation(int i) {
        return pictureFileName;
    }

    public boolean doSpread() {
        return spread;
    }

    public double getHardScale() {
        return 0;
    }
}
