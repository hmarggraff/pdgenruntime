// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.env.Res;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: patrick
 * Date: Nov 30, 2004
 * Time: 5:21:46 PM
 */
public class ImageDetection {
    public static final ImageClass PNG = new ImageClass(".png", "image/png");
    public static final ImageClass GIF = new ImageClass(".gif", "image/gif");
    public static final ImageClass JPEG = new ImageClass(".jpg", "image/jpeg");
    public static final ImageClass BAD = new ImageClass(".no-image", "application/byte");

    static {
        ImageIO.setUseCache(false);
    }

    public static ImageClass detectImageClass(byte[] rawData) {
        int c1 = rawData[0];
        int c2 = rawData[1];
        if (c1 == -1 && c2 == -40) {
            return JPEG;
        }
        if (c1 == PdfPng.PNGID[0] && c2 == PdfPng.PNGID[1]) {
            return PNG;
        }
        if (c1 == PdfGif.gifMagic[0] && c2 == PdfGif.gifMagic[1]) {
            return GIF;
        }
        return null;
    }

    public static ImageHolder recodeImage(byte[] rawData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(rawData);
        BufferedImage bimg = ImageIO.read(bais);
        if (bimg == null)
            return null;
        ByteArrayOutputStream baosPng = new ByteArrayOutputStream(rawData.length);
        ImageIO.write(bimg, "png", baosPng);
        return new ImageHolder(baosPng.toByteArray(), PNG);
    }

    public static ImageHolder recodeIcon(Icon input) throws IOException {
        Image img;
        if (input instanceof ImageIcon)
            img = ((ImageIcon) input).getImage();
        else if (input != null) {
            if (input.getIconWidth() == -1 || input.getIconHeight() == -1)
                throw new IOException(Res.str("Could_not_load_image"));
            img = new BufferedImage(input.getIconWidth(), input.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2 = ((BufferedImage) img).createGraphics();
            input.paintIcon(null, g2, 0, 0);
        } else {
            throw new JoriaAssertionError("oops");
        }
        if (!(img instanceof RenderedImage)) {
            Component comp = new Component() {
            };
            MediaTracker track = new MediaTracker(comp);
            track.addImage(img, 1);
            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                throw new IOException(Res.str("Could_not_load_image"));
            }
            if (track.isErrorID(1)) {
                return new ImageHolder(null, BAD);
            }
            BufferedImage bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bufImg.createGraphics();
            g2.drawImage(img, null, null);
            img = bufImg;
        }
        ByteArrayOutputStream baosPng = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) img, "png", baosPng);
        return new ImageHolder(baosPng.toByteArray(), PNG);
    }

    public static class ImageHolder {
        private final byte[] imageData;
        private final ImageClass imageClass;
        private boolean badImage;

        public byte[] getImageData() {
            return imageData;
        }

        public ImageClass getImageClass() {
            return imageClass;
        }

        private ImageHolder(byte[] imageData, ImageClass imageClass) {
            this.imageData = imageData;
            this.imageClass = imageClass;
            if (imageClass.equals(BAD))
                badImage = true;
        }

        public boolean isBadImage() {
            return badImage;
        }
    }

    public static class ImageClass {
        private final String defaultExtention;
        private final String defaultMimeType;

        private ImageClass(String ext, String mime) {
            defaultExtention = ext;
            defaultMimeType = mime;
        }

        public String getDefaultExtention() {
            return defaultExtention;
        }

        public String getDefaultMimeType() {
            return defaultMimeType;
        }
    }
}
