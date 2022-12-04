// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: May 23, 2003
 * Time: 3:44:39 PM
 * To change this template use Options | File Templates.
 */
public class ConvertImageToByteArray
{
    public static final Object[]  nullObjectArray = null;
    // This method returns true if the specified image has transparent pixels
     public static boolean hasAlpha(Image image) {
         // If buffered image, the color model is readily available
         if (image instanceof BufferedImage) {
             BufferedImage bimage = (BufferedImage)image;
             return bimage.getColorModel().hasAlpha();
         }

         // Use a pixel grabber to retrieve the image's color model;
         // grabbing a single pixel is usually sufficient
          PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        //noinspection EmptyCatchBlock
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

         // Get the image's color model
         ColorModel cm = pg.getColorModel();
         return cm.hasAlpha();
     }

    // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e665 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage;

        // Create a buffered image using the default color model
        int type = BufferedImage.TYPE_INT_RGB;
        if (hasAlpha) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    public static byte[] convertImage(Image img)
    {
        img = toBufferedImage(img);

        byte[] outBytes = null;

        //noinspection EmptyCatchBlock
        try
        {
            Class<?> imageioClazz = Class.forName("javax.imageio.ImageIO");
            Method getWriter = imageioClazz.getMethod("getImageWritersByMIMEType", String.class);
            Iterator<?> it = (Iterator<?>)getWriter.invoke(null, "image/png");
            if(it.hasNext())
            {
                Object iw = it.next();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                Class<?> mcios = Class.forName("javax.imageio.stream.MemoryCacheImageOutputStream");
                Constructor<?> mciosCT = mcios.getConstructor(OutputStream.class);
                Object str = mciosCT.newInstance(baos);

                Method setOutput = iw.getClass().getMethod("setOutput", Object.class);

                setOutput.invoke(iw, str);

                Method write = iw.getClass().getMethod("write", RenderedImage.class);

                write.invoke(iw, img);

                Method close = mcios.getMethod("close");

                close.invoke(str, nullObjectArray);

                outBytes = baos.toByteArray();
            }
        }
        catch (Exception e)
        {
        }

        if(outBytes == null)
            //noinspection EmptyCatchBlock
            try
            {
                Class<?> pngEncoder = Class.forName("com.keypoint.PngEncoderB");
                Constructor<?> pngEncoderCT = pngEncoder.getConstructor(BufferedImage.class);
                Object png = pngEncoderCT.newInstance(img);
                Method setCompressionLevel = pngEncoder.getMethod("setCompressionLevel", int.class);
                setCompressionLevel.invoke(png, 0x9);
                Method pngEncode = pngEncoder.getMethod("pngEncode");
                outBytes = (byte[]) pngEncode.invoke(png, nullObjectArray);

            }
            catch (Exception e)
            {
            }

        return outBytes;
    }
}
