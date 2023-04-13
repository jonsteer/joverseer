package org.joverseer.ui.map.renderers;

import org.springframework.richclient.image.ImageSource;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.awt.image.*;
import java.awt.*;

/**
 * Base class for renderers that use SVG Document 
 * Provides utility methods
 * 
 * @author Marios Skounakis
 * @author Jon Steer
 */
public abstract class SVGRenderer extends AbstractBaseRenderer {

    static Logger logger = Logger.getLogger(ImageRenderer.class);

    // injected dependencies
    ImageSource imgSource;
    
    @Override
    public boolean isSVGReady() {
    	return true;
    }

	public ImageSource getImgSource() {
		return this.imgSource;
	}

	public void setImgSource(ImageSource imgSource) {
		this.imgSource = imgSource;
	}


    /**
     * Replace one color with the other
     */
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

    /**
     * Used to generate the effect of dashed lines to designate hidden pop centers
     */
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

	@Override
	public void refreshConfig() {
		// TODO Auto-generated method stub
		
	}

}
