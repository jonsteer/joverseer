package org.joverseer.ui.map.renderers;

import org.springframework.richclient.image.ImageSource;
import org.springframework.richclient.application.Application;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.awt.image.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mskounak
 * Date: 6 ��� 2006
 * Time: 8:28:35 ��
 * To change this template use File | Settings | File Templates.
 */
public abstract class ImageRenderer implements Renderer {
    protected HashMap images = new HashMap();

    static Logger logger = Logger.getLogger(PopulationCenterRenderer.class);

    public BufferedImage copyImage(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(),
                image.getHeight(),
                image.getType());
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return newImage;
    }

    protected BufferedImage getImage(String imgName) {
        if (!images.containsKey(imgName)) {
            try {
                ImageSource imgSource = (ImageSource) Application.instance().getApplicationContext().getBean("imageSource");
                Image img = imgSource.getImage(imgName);
                BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics g = bimg.getGraphics();
                g.drawImage(img, 0, 0, null);
                g.dispose();
                //img = makeColorTransparent(img, Color.white);
                images.put(imgName, bimg);
                return bimg;
            }
            catch (Exception exc) {
                logger.error(String.format("Error %s loading image %s.", exc.getMessage(), imgName));
            }
        }
        return (BufferedImage) images.get(imgName);
    }

    public static Image makeColorTransparent(Image im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {
            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    protected void changeColor(BufferedImage src, Color remove, Color replace) {
        int w = src.getWidth();
        int h = src.getHeight();
        int rgbRemove = remove.getRGB();
        int rgbReplace = replace.getRGB();
        // Copy pixels a scan line at a time
        int buf[] = new int[w];
        for (int y = 0; y < h; y++) {
            src.getRGB(0, y, w, 1, buf, 0, w);
            for (int x = 0; x < w; x++) {
                if (buf[x] == rgbRemove) {
                    buf[x] = rgbReplace;
                }
            }
            src.setRGB(0, y, w, 1, buf, 0, w);
        }
    }

    protected void makeHidden(BufferedImage src, Color remove, Color replace) {
        int w = src.getWidth();
        int h = src.getHeight();
        int rgbRemove = remove.getRGB();
        int rgbReplace = replace.getRGB();
        // Copy pixels a scan line at a time
        int buf[] = new int[w];
        for (int y = 0; y < h; y++) {
            src.getRGB(0, y, w, 1, buf, 0, w);
            for (int x = 0; x < w; x++) {
                if ((x + y) % 3 == 0 && buf[x] == rgbRemove) {
                    buf[x] = rgbReplace;
                }
            }
            src.setRGB(0, y, w, 1, buf, 0, w);
        }
    }

}
