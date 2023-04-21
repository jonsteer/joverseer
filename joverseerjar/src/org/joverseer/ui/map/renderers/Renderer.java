package org.joverseer.ui.map.renderers;

import java.awt.*;

import org.joverseer.ui.map.JOSVGMap;
import org.w3c.dom.svg.SVGDocument;

/**
 * Interface for all renderers
 * 
 * @author Marios Skounakis
 */
public interface Renderer {
	public void refreshConfig();
    public boolean appliesTo(Object obj);
	
	public boolean isSVGReady();
	
    public void render(Object obj, Graphics2D g, int x, int y);
    public void render(Object obj, JOSVGMap s, int x, int y);
}
